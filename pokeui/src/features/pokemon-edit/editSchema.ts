import { z } from 'zod';

/**
 * US04 – client-side mirror of the backend @Valid rules on UpdatePokemonRequest:
 * localizedName ≤ 60 chars, geographicMetadata ≤ 255 chars,
 * tags: at most 10, each ≤ 30 chars, non-blank.
 */
export const editSchema = z.object({
  localizedName: z.string().trim().max(60, 'Local name must be at most 60 characters').optional().or(z.literal('')),
  geographicMetadata: z.string().trim().max(255, 'Geographic metadata must be at most 255 characters').optional().or(z.literal('')),
  internalClassificationTags: z
    .array(z.string().trim().min(1, 'Tag cannot be empty').max(30, 'Tag must be at most 30 characters'))
    .max(10, 'At most 10 tags allowed'),
});

export type EditForm = z.infer<typeof editSchema>;
