// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

export interface PokemonSummary {
  id: number;            // PokeAPI numeric id
  name: string;
  sprite: string;
  category: string | null;
  mass: number | null;
  skills: string[];
}

export interface PageResult<T> {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  content: T[];  // Backend uses "content" not "items"
}
export interface Statistics {
  hp: number;
  attack: number;
  defense: number;
  specialAttack: number;
  specialDefense: number;
  speed: number;
}

export interface EvolutionStage {
  stage: number;
  pokeApiId: number;
  name: string;
  sprite: string;
}

export interface PokemonDetail {
  id: number;
  name: string;
  image: string;
  statistics: Statistics;
  narrativeDescription: string | null;
  evolutionaryLineage: EvolutionStage[];
  syncedLocally: boolean;
  localUuid: string | null;
}
export interface LocalPokemon {
  uuid: string;
  pokeapiId: number;
  name: string;
  spriteUrl: string;
  localizedName: string | null;
  geographicMetadata: string | null;
  internalClassificationTags: string[];
  version: number; // optimistic-locking token, must be echoed back on PUT
}
export interface AuthResponse {
  token: string;
  email: string;
}
export interface ApiError {
  status: number;
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
