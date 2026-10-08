import { useEffect, useRef } from 'react';
import { z } from 'zod';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchLocalPokemon, toApiError, updateLocalPokemon } from '../../lib/api';
import { queryKeys } from '../../lib/queryKeys';
import { editSchema } from './editSchema';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Skeleton } from '../../components/ui/Skeleton';
import { useToast } from '../../components/ui/useToast';

// One concrete form type (schema fields + optimistic-locking token) keeps the
// ZodResolver and useForm generics in sync – avoids RHF's TFieldValues widening.
const editWithVersionSchema = editSchema.extend({ version: z.number() });
type EditFormV = z.infer<typeof editSchema> & { version: number };

/**
 * US04 – edit form for a locally-synced Pokémon.
 * The optimistic-locking `version` token is carried in a hidden field and
 * echoed back on PUT; a 409 (stale version) triggers a refetch + toast.
 */
export function PokemonEditPage() {
  const { uuid = '' } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();
  const queryClient = useQueryClient();

  const justSynced = Boolean((location.state as { justSynced?: boolean } | null)?.justSynced);
  const justSyncedShownRef = useRef(false);

  const { data: local, isPending, error } = useQuery({
    queryKey: queryKeys.localPokemon(uuid),
    queryFn: () => fetchLocalPokemon(uuid),
    enabled: !!uuid, // Only fetch when we have a valid UUID from the route
  });

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<EditFormV>({ resolver: zodResolver(editWithVersionSchema) });

  // Populate the form once the record arrives.
  useEffect(() => {
    if (local) {
      reset({
        localizedName: local.localizedName ?? '',
        geographicMetadata: local.geographicMetadata ?? '',
        internalClassificationTags: local.internalClassificationTags ?? [],
        version: local.version,
      });
      if (justSynced && !justSyncedShownRef.current) {
        toast.push('success', `${local.name} added to your local Pokédex – customise it below.`);
        justSyncedShownRef.current = true;
      }
    }
  }, [local, reset, justSynced, toast]);

  const mutation = useMutation({
    mutationFn: (values: EditFormV) =>
      updateLocalPokemon(uuid, {
        localizedName: values.localizedName || undefined,
        geographicMetadata: values.geographicMetadata || undefined,
        internalClassificationTags: values.internalClassificationTags,
        version: values.version,
      }),
    onSuccess: (updated) => {
      queryClient.setQueryData(queryKeys.localPokemon(uuid), updated);
      queryClient.invalidateQueries({ queryKey: ['pokemon'] });
      toast.push('success', 'Changes saved.');
      navigate(`/pokemon/${updated.pokeapiId}`, { replace: true });
    },
    onError: (err) => {
      const apiErr = toApiError(err);
      if (apiErr.status === 409) {
        toast.push('error', 'Someone edited this entry first – reloading latest version.');
        queryClient.invalidateQueries({ queryKey: queryKeys.localPokemon(uuid) });
      } else if (apiErr.fieldErrors) {
        toast.push('error', Object.values(apiErr.fieldErrors)[0]);
      } else {
        toast.push('error', apiErr.message);
      }
    },
  });

  const tags: string[] = watch('internalClassificationTags') ?? [];

  if (isPending) {
    return (
      <main className="mx-auto max-w-xl px-4 py-8">
        <Skeleton className="h-8 w-40" />
        <Skeleton className="mt-4 h-64 w-full rounded-xl" />
      </main>
    );
  }

  if (error || !local) {
    const apiErr = toApiError(error);
    return (
      <main className="mx-auto max-w-xl px-4 py-8">
        <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-6 text-rose-700">
          <h2 className="font-bold">{apiErr.status === 404 ? 'Local entry not found' : 'Failed to load entry'}</h2>
          <p className="mt-1 text-sm">{apiErr.message}</p>
        </div>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-xl px-4 py-8">
      <div className="mb-6 flex items-center gap-4">
        <img src={`https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${local.pokeapiId}.png`} alt={local.name} width={64} height={64} className="h-16 w-16 [image-rendering:pixelated]" />
        <div>
          <h1 className="text-2xl font-extrabold capitalize">Edit {local.name}</h1>
          <p className="text-xs text-slate-500">local record v{local.version}</p>
        </div>
      </div>

      <Card>
        <form onSubmit={handleSubmit((v: EditFormV) => mutation.mutate(v))} noValidate>
          <input type="hidden" {...register('version', { valueAsNumber: true })} />
          <Input label="Localised name" maxLength={60} placeholder={`e.g. "Mysterious Bulba"`} error={errors.localizedName?.message} {...register('localizedName')} />
          <Input label="Geographic metadata" maxLength={255} placeholder="e.g. Viridian Forest, Kanto" error={errors.geographicMetadata?.message} {...register('geographicMetadata')} />

          {/* Tag editor: comma-free UX – add/remove chips, validated by Zod on submit */}
          <fieldset className="mb-4">
            <legend className="mb-1 block text-sm font-medium text-slate-700">Internal classification tags</legend>
            <div className="flex flex-wrap gap-2">
              {tags.map((tag, i) => (
                <span key={`${tag}-${i}`} className="flex items-center gap-1 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
                  {tag}
                  <button
                    type="button"
                    aria-label={`Remove tag ${tag}`}
                    onClick={() => setValue('internalClassificationTags', tags.filter((_, idx) => idx !== i), { shouldDirty: true })}
                    className="text-slate-400 hover:text-red-600"
                  >
                    ✕
                  </button>
                </span>
              ))}
            </div>
            <div className="mt-2 flex gap-2">
              <input
                id="new-tag"
                placeholder="Add a tag…"
                maxLength={30}
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-300"
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    const val = (e.target as HTMLInputElement).value.trim();
                    if (val) {
                      setValue('internalClassificationTags', [...tags, val], { shouldDirty: true });
                      (e.target as HTMLInputElement).value = '';
                    }
                  }
                }}
              />
            </div>
            {errors.internalClassificationTags && (
              <p role="alert" className="mt-1 text-xs font-medium text-red-600">{errors.internalClassificationTags.message}</p>
            )}
          </fieldset>

          <div className="flex gap-3">
            <Button type="submit" loading={mutation.isPending}>Save changes</Button>
            <Button type="button" variant="secondary" onClick={() => {
              const savedPage = sessionStorage.getItem('pokeapp:lastListPage');
              const page = savedPage ? Number(savedPage) : 0;
              navigate(`/?page=${page}`);
            }}>Cancel</Button>
          </div>
        </form>
      </Card>
    </main>
  );
}
