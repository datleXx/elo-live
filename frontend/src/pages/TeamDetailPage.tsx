import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { fetchTeamRatingHistory, fetchTeamDivisionHistory, ApiError } from "@/lib/api";
import type { RatingUpdate, TeamDivisionSpell } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { EloChart } from "@/components/teams/EloChart";
import { DivisionHistory } from "@/components/teams/DivisionHistory";
import { Button } from "@/components/ui/button";

export function TeamDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [history, setHistory] = useState<RatingUpdate[] | null>(null);
  const [divisions, setDivisions] = useState<TeamDivisionSpell[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setHistory(null);
    setDivisions(null);
    setError(null);
    const teamId = Number(id);
    Promise.all([fetchTeamRatingHistory(teamId), fetchTeamDivisionHistory(teamId)])
      .then(([ratings, divs]) => {
        setHistory(ratings);
        setDivisions(divs);
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : "Failed to load"));
  }, [id]);

  const teamName = history?.[0]?.teamName;
  const latest = history?.at(-1);

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-4">
      <Button asChild variant="ghost" size="sm" className="w-fit -ml-2">
        <Link to="/teams">
          <ArrowLeft className="size-4" />
          All teams
        </Link>
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>{teamName ?? (error ? "Team" : <Skeleton className="h-6 w-32" />)}</CardTitle>
          {latest && (
            <p className="text-sm text-muted-foreground">
              Current rating{" "}
              <span className="font-semibold text-foreground">
                {Math.round(latest.rating)}
              </span>{" "}
              as of {latest.matchDate}
            </p>
          )}
        </CardHeader>
        <CardContent>
          {error && <p className="text-sm text-destructive">{error}</p>}
          {!error && !history && <Skeleton className="h-80 w-full" />}
          {!error && history && history.length === 0 && (
            <p className="py-10 text-center text-sm text-muted-foreground">
              No rating history for this team yet.
            </p>
          )}
          {!error && history && history.length > 0 && <EloChart history={history} />}
        </CardContent>
      </Card>

      {!error && divisions && divisions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Division history</CardTitle>
          </CardHeader>
          <CardContent>
            <DivisionHistory spells={divisions} />
          </CardContent>
        </Card>
      )}
    </div>
  );
}
