import axios, { AxiosError } from 'axios';
import type {
  ApiError,
  AuthResponse,
  LocalPokemon,
  PageResult,
  PokemonDetail,
  PokemonSummary,
} from './types';

/**
 * Single Axios instance for the whole app.
 * - baseURL '/api' works both in dev (Vite proxy) and prod (nginx proxy).
 * - Request interceptor attaches the JWT from localStorage.
 * - Response interceptor normalises backend errors into ApiError and clears
 *   the token on 401 so the UI can bounce the user to /login.
 */
export const TOKEN_KEY = import.meta.env.VITE_TOKEN_KEY;

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function storeToken(token: string | null): void {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export const http = axios.create({ baseURL: '/api', timeout: 30000 });

http.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (error: AxiosError) => {
    if (error.response?.status === 401) storeToken(null);
    return Promise.reject(error);
  },
);

/** Extract the uniform error envelope thrown by GlobalExceptionHandler. */
export function toApiError(err: unknown): ApiError {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as Partial<ApiError> | undefined;
    return {
      status: err.response?.status ?? 0,
      error: data?.error ?? 'NETWORK_ERROR',
      message: data?.message ?? 'Unable to reach the PokéManager API.',
      fieldErrors: data?.fieldErrors,
    };
  }
  return { status: 0, error: 'UNKNOWN', message: String(err) };
}

// ---- Auth endpoints -------------------------------------------------------
export async function apiRegister(username: string, email: string, password: string): Promise<AuthResponse> {
  const { data } = await http.post<AuthResponse>('/auth/register', { username, email, password });
  return data;
}

export async function apiLogin(email: string, password: string): Promise<AuthResponse> {
  const { data } = await http.post<AuthResponse>('/auth/login', { email, password });
  return data;
}

// ---- US01/US02 public endpoints -------------------------------------------
export async function fetchPokemonList(page: number, size: number): Promise<PageResult<PokemonSummary>> {
  const { data } = await http.get<PageResult<PokemonSummary>>('/public/pokemon', {
    params: { page, size },
  });
  return data;
}

export async function fetchPokemonDetail(idOrName: string): Promise<PokemonDetail> {
  const { data } = await http.get<PokemonDetail>(`/public/pokemon/${idOrName}`);
  return data;
}

// ---- US03/US04 protected endpoints ----------------------------------------
export async function syncPokemon(pokeApiId: number): Promise<LocalPokemon> {
  const { data } = await http.post<LocalPokemon>(`/protected/pokemon/${pokeApiId}/sync`);
  return data;
}

export interface UpdatePayload {
  localizedName?: string;
  geographicMetadata?: string;
  internalClassificationTags?: string[];
  version: number; // required – optimistic locking
}

export async function fetchLocalPokemon(uuid: string): Promise<LocalPokemon> {
  const { data } = await http.get<LocalPokemon>(`/protected/pokemon/${uuid}`);
  return data;
}

export async function updateLocalPokemon(uuid: string, payload: UpdatePayload): Promise<LocalPokemon> {
  const { data } = await http.put<LocalPokemon>(`/protected/pokemon/${uuid}`, payload);
  return data;
}
