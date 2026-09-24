import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchTeams, fetchTeamCompetitions, ApiError } from "@/lib/api";
import { countryOf, NATIONS } from "@/lib/divisions";
import type { Team } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";

export function TeamsPage() {
  const [teams, setTeams] = useState<Team[] | null>(null);
  const [countryByTeam, setCountryByTeam] = useState<Map<number, string> | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState("");

  useEffect(() => {
    Promise.all([fetchTeams(), fetchTeamCompetitions()])
      .then(([teamList, competitions]) => {
        setTeams(teamList);
        const map = new Map<number, string>();
        for (const tc of competitions) {
          const country = countryOf(tc.competition);
          if (country) map.set(tc.teamId, country);
        }
        setCountryByTeam(map);
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : "Failed to load"));
  }, []);

  const filtered = teams?.filter((t) => t.name.toLowerCase().includes(query.toLowerCase()));

  // Group filtered teams by country, in NATIONS order, with an "Other" bucket
  // for anything we couldn't place (no matches ingested for them yet).
  const groups: { country: string; teams: Team[] }[] = [];
  if (filtered && countryByTeam) {
    const countryOrder = [...NATIONS.map((n) => n.country), "Other"];
    const byCountry = new Map<string, Team[]>();
    for (const t of filtered) {
      const country = countryByTeam.get(t.id) ?? "Other";
      if (!byCountry.has(country)) byCountry.set(country, []);
      byCountry.get(country)!.push(t);
    }
    for (const country of countryOrder) {
      const list = byCountry.get(country);
      if (list && list.length > 0) {
        groups.push({ country, teams: [...list].sort((a, b) => a.name.localeCompare(b.name)) });
      }
    }
  }

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold tracking-tight">Teams</h1>
        <Input
          className="w-56"
          placeholder="Search teams…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      {error && (
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-destructive">{error}</p>
          </CardContent>
        </Card>
      )}

      {!error && !teams && (
        <Card>
          <CardContent className="grid grid-cols-2 gap-2 pt-6 sm:grid-cols-3">
            {Array.from({ length: 12 }).map((_, i) => (
              <Skeleton key={i} className="h-9 w-full" />
            ))}
          </CardContent>
        </Card>
      )}

      {!error && teams && groups.length === 0 && (
        <Card>
          <CardContent className="pt-6">
            <p className="py-10 text-center text-sm text-muted-foreground">
              No teams yet — ingest a season first.
            </p>
          </CardContent>
        </Card>
      )}

      {!error &&
        groups.map((group) => (
          <Card key={group.country}>
            <CardHeader>
              <CardTitle className="flex items-baseline gap-2">
                {group.country}
                <span className="text-xs font-normal text-muted-foreground">
                  {group.teams.length} team{group.teams.length === 1 ? "" : "s"}
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                {group.teams.map((t) => (
                  <Link
                    key={t.id}
                    to={`/teams/${t.id}`}
                    className="rounded-md border px-3 py-2 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground"
                  >
                    {t.name}
                  </Link>
                ))}
              </div>
            </CardContent>
          </Card>
        ))}
    </div>
  );
}
