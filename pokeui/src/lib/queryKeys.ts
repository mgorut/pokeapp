// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

export const queryKeys = {
  pokemonList: (page: number, size: number) => ['pokemon', 'list', page, size] as const,
  pokemonDetail: (idOrName: string) => ['pokemon', 'detail', idOrName] as const,
  localPokemon: (uuid: string) => ['pokemon', 'local', uuid] as const,
};

export const PAGE_SIZE = 12;
