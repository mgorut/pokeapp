/**
 * Query key factory – centralised so invalidation stays consistent
 * (e.g. after a sync we invalidate both the list and the detail views).
 */
export const queryKeys = {
  pokemonList: (page: number, size: number) => ['pokemon', 'list', page, size] as const,
  pokemonDetail: (idOrName: string) => ['pokemon', 'detail', idOrName] as const,
  localPokemon: (uuid: string) => ['pokemon', 'local', uuid] as const,
};

export const PAGE_SIZE = 12;
