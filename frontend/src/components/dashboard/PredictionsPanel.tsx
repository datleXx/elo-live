import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { LivePrediction } from "@/types";

function pct(v: number | null): string {
  return v == null ? "—" : `${Math.round(v * 100)}%`;
}

/** A compact three-segment bar: home / draw / away probability, proportional. */
function ProbabilityBar({ home, draw, away }: { home: number; draw: number; away: number }) {
  return (
    <div className="flex h-1.5 w-full overflow-hidden rounded-full bg-muted">
      <div className="bg-up" style={{ width: `${home * 100}%` }} />
      <div className="bg-muted-foreground/40" style={{ width: `${draw * 100}%` }} />
      <div className="bg-down" style={{ width: `${away * 100}%` }} />
    </div>
  );
}

interface Props {
  predictions: LivePrediction[];
  connected: boolean;
}

export function PredictionsPanel({ predictions, connected }: Props) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>Upcoming</CardTitle>
        <Badge variant={connected ? "default" : "secondary"} className="gap-1.5">
          <span
            className={`size-1.5 rounded-full ${connected ? "bg-up" : "bg-muted-foreground"}`}
          />
          {connected ? "connected" : "disconnected"}
        </Badge>
      </CardHeader>
      <CardContent>
        {predictions.length === 0 ? (
          <p className="py-10 text-center text-sm text-muted-foreground">
            No pending predictions — once a team's rating updates and they
            have an upcoming fixture, it'll appear here until that fixture
            is actually played, then move into Rounds with a verdict.
          </p>
        ) : (
          <ul className="max-h-[28rem] space-y-2 overflow-y-auto">
            {predictions.map((p) => (
              <li
                key={`${p.homeTeam}|${p.awayTeam}`}
                className="space-y-1.5 rounded-md border px-3 py-2.5"
              >
                <div className="flex items-baseline justify-between gap-2 text-base">
                  <span className="min-w-0 truncate font-medium">
                    {p.homeTeam} <span className="text-muted-foreground">vs</span> {p.awayTeam}
                  </span>
                  <span className="shrink-0 text-xs text-muted-foreground">{p.matchDate}</span>
                </div>
                <ProbabilityBar home={p.homeWinProb} draw={p.drawProb} away={p.awayWinProb} />
                <div className="flex items-center justify-between text-xs text-muted-foreground">
                  <span>
                    Elo {pct(p.homeWinProb)} / {pct(p.drawProb)} / {pct(p.awayWinProb)}
                  </span>
                  <span>
                    Market {pct(p.marketHomeWinProb)} / {pct(p.marketDrawProb)} /{" "}
                    {pct(p.marketAwayWinProb)}
                  </span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
