import { useEffect, useState } from "react";
import { fetchCurrentRatings, ApiError } from "@/lib/api";
import type { RatingUpdate } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { DivisionSelect } from "@/components/DivisionSelect";

export function RatingsPage() {
  const [division, setDivision] = useState("E0");
  const [ratings, setRatings] = useState<RatingUpdate[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setRatings(null);
    setError(null);
    fetchCurrentRatings(division)
      .then((data) => {
        if (!cancelled) setRatings(data);
      })
      .catch((e) => {
        if (!cancelled) setError(e instanceof ApiError ? e.message : "Failed to load");
      });
    return () => {
      cancelled = true;
    };
  }, [division]);

  const sorted = ratings ? [...ratings].sort((a, b) => b.rating - a.rating) : null;

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold tracking-tight">Standings</h1>
        <DivisionSelect value={division} onValueChange={setDivision} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Current Elo ratings</CardTitle>
        </CardHeader>
        <CardContent>
          {error && <p className="text-sm text-destructive">{error}</p>}
          {!error && !sorted && (
            <div className="space-y-2">
              {Array.from({ length: 8 }).map((_, i) => (
                <Skeleton key={i} className="h-8 w-full" />
              ))}
            </div>
          )}
          {!error && sorted && sorted.length === 0 && (
            <p className="py-10 text-center text-sm text-muted-foreground">
              No ratings yet for this competition — ingest a season first.
            </p>
          )}
          {!error && sorted && sorted.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-10">#</TableHead>
                  <TableHead>Team</TableHead>
                  <TableHead className="text-right">Rating</TableHead>
                  <TableHead className="text-right">As of</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {sorted.map((r, i) => (
                  <TableRow key={r.teamId}>
                    <TableCell className="text-muted-foreground">{i + 1}</TableCell>
                    <TableCell className="font-medium">{r.teamName}</TableCell>
                    <TableCell className="text-right font-semibold tabular-nums">
                      {Math.round(r.rating)}
                    </TableCell>
                    <TableCell className="text-right text-muted-foreground">
                      {r.matchDate}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
