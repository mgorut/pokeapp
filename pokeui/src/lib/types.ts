/**
 * API types mirroring the Spring Boot DTOs (pokeapi presentation/web/ResponseDtos).
 * Keeping them hand-written (not generated) since the backend contract is small and stable.
 */

// US01 – list item returned by GET /api/public/pokemon
export interface PokemonSummary {
  id: number;            // PokeAPI numeric id
  uuid: string | null;   // local uuid when already synced, null otherwise
  name: string;
  spriteUrl: string;
  category: string | null;
  massKg: number | null;
  skills: string[];
}

export interface PageResult<T> {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  items: T[];
}

// US02 – detail returned by GET /api/public/pokemon/{idOrName}
export interface Statistic {
  name: string;
  value: number;
}

export interface EvolutionStage {
  id: number;
  name: string;
  spriteUrl: string;
  minLevel: number | null;
}

export interface PokemonDetail {
  id: number;
  name: string;
  spriteUrl: string;
  category: string | null;
  massKg: number | null;
  heightM: number | null;
  description: string | null;
  skills: string[];
  statistics: Statistic[];
  evolutionLine: EvolutionStage[];
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
