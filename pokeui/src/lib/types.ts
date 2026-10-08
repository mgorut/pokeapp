/**
 * API types mirroring the Spring Boot DTOs (pokeapi presentation/web/ResponseDtos).
 * Keeping them hand-written (not generated) since the backend contract is small and stable.
 */

// US01 – list item returned by GET /api/public/pokemon
// Matches the backend PokemonSummaryDto field names
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

// US02 – detail returned by GET /api/public/pokemon/{idOrName}
// Matches backend PokemonDetailDto field names
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

// US03/US04 – local record
export interface LocalPokemon {
  uuid: string;
  pokeApiId: number;
  name: string;
  spriteUrl: string;
  localizedName: string | null;
  geographicMetadata: string | null;
  internalClassificationTags: string[];
  version: number; // optimistic-locking token, must be echoed back on PUT
}

// Auth
export interface AuthResponse {
  token: string;
  email: string;
}

// Uniform error envelope produced by GlobalExceptionHandler
export interface ApiError {
  status: number;
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
