import type { RatingUpdate, Team, TeamCompetition, TeamDivisionSpell } from "@/types";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function post(path: string, params: Record<string, string | number>) {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).map(([k, v]) => [k, String(v)])),
  );
  const res = await fetch(`${BASE_URL}${path}?${query.toString()}`, {
    method: "POST",
  });
  const text = await res.text();
  if (!res.ok) {
    throw new ApiError(res.status, text || res.statusText);
  }
  return text;
}

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`);
  if (!res.ok) {
    const text = await res.text();
    throw new ApiError(res.status, text || res.statusText);
  }
  return res.json();
}

export function ingest(league: string, season: string) {
  return post("/api/ingest", { league, season });
}

export function startReplay(league: string, season: string, delayMillis: number) {
  return post("/api/replay/start", { league, season, delayMillis });
}

export function stopReplay() {
  return post("/api/replay/stop", {});
}

export function replayStatus(): Promise<boolean> {
  return get<boolean>("/api/replay/status");
}

export function fetchTeams(): Promise<Team[]> {
  return get<Team[]>("/api/teams");
}

export function fetchTeamRatingHistory(teamId: number): Promise<RatingUpdate[]> {
  return get<RatingUpdate[]>(`/api/teams/${teamId}/ratings`);
}

export function fetchTeamDivisionHistory(teamId: number): Promise<TeamDivisionSpell[]> {
  return get<TeamDivisionSpell[]>(`/api/teams/${teamId}/divisions`);
}

export function fetchTeamCompetitions(): Promise<TeamCompetition[]> {
  return get<TeamCompetition[]>("/api/teams/competitions");
}

export function fetchCurrentRatings(competition: string): Promise<RatingUpdate[]> {
  return get<RatingUpdate[]>(`/api/ratings/${competition}`);
}
