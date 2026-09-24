import { Check, X } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import type { ResolvedResult, Round } from "@/lib/ws";

function RatingDelta({ before, after }: { before: number; after: number }) {
  const delta = Math.round(after) - Math.round(before);
  const sign = delta > 0 ? "+" : "";
  const color = delta > 0 ? "text-up" : delta < 0 ? "text-down" : "text-muted-foreground";
  return (
    <span className={`tabular-nums ${color}`}>
      {Math.round(after)}
      <span className="ml-1 text-xs">
        ({sign}
        {delta})
      </span>
    </span>
  );
}

function Verdict({ result }: { result: ResolvedResult }) {
  if (result.predictedOutcome === null) {
    return <span className="text-xs text-muted-foreground/50">no prediction</span>;
  }
  return result.predictionCorrect ? (
    <Badge variant="outline" className="gap-1 border-up/40 text-up">
      <Check className="size-3" />
      predicted
    </Badge>
  ) : (
    <Badge variant="outline" className="gap-1 border-down/40 text-down">
      <X className="size-3" />
      predicted {result.predictedOutcome}
    </Badge>
  );
}

function ResultRow({ result }: { result: ResolvedResult }) {
  return (
    <li className="space-y-1.5 rounded-md border px-3 py-2.5">
      <div className="flex items-center gap-2 text-base">
        <span className="min-w-0 flex-1 truncate font-medium">{result.homeTeam}</span>
        <span className="shrink-0 rounded bg-muted px-2 py-0.5 text-sm font-semibold tabular-nums">
          {result.homeGoals} – {result.awayGoals}
        </span>
        <span className="min-w-0 flex-1 truncate text-right font-medium">{result.awayTeam}</span>
      </div>
      <div className="flex items-center justify-between gap-2 text-xs">
        <div className="flex items-center gap-3 text-muted-foreground">
          <RatingDelta before={result.homeRatingBefore} after={result.homeRatingAfter} />
          <span>·</span>
          <RatingDelta before={result.awayRatingBefore} after={result.awayRatingAfter} />
        </div>
        <Verdict result={result} />
      </div>
    </li>
  );
}

interface Props {
  rounds: Round[];
  connected: boolean;
}

export function RoundsFeed({ rounds, connected }: Props) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>Rounds</CardTitle>
        <Badge variant={connected ? "default" : "secondary"} className="gap-1.5">
          <span
            className={`size-1.5 rounded-full ${connected ? "bg-up" : "bg-muted-foreground"}`}
          />
          {connected ? "connected" : "disconnected"}
        </Badge>
      </CardHeader>
      <CardContent>
        {rounds.length === 0 ? (
          <p className="py-10 text-center text-sm text-muted-foreground">
            No results yet — trigger an ingest or start a replay to watch
            rounds come in here as they're processed.
          </p>
        ) : (
          <div className="max-h-[32rem] space-y-4 overflow-y-auto">
            {rounds.map((round, i) => (
              <div key={round.matchDate}>
                {i > 0 && <Separator className="mb-4" />}
                <div className="mb-2 flex items-baseline justify-between">
                  <h3 className="text-sm font-semibold text-foreground">
                    Round {round.roundNumber}
                  </h3>
                  <span className="text-xs text-muted-foreground">{round.matchDate}</span>
                </div>
                <ul className="space-y-1">
                  {round.results.map((r) => (
                    <ResultRow key={`${r.homeTeam}-${r.awayTeam}`} result={r} />
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
