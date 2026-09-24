import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { ArrowDown, ArrowUp, Minus } from "lucide-react";
import type { MatchResult } from "@/types";

interface StandingRow {
  teamName: string;
  rating: number;
}

type Movement = "up" | "down" | "same";

/** Feed is newest-first, so the first time a team is seen is its latest state. */
function deriveStandings(feed: MatchResult[]): StandingRow[] {
  const seen = new Map<string, StandingRow>();
  for (const r of feed) {
    if (!seen.has(r.homeTeam)) {
      seen.set(r.homeTeam, { teamName: r.homeTeam, rating: r.homeRatingAfter });
    }
    if (!seen.has(r.awayTeam)) {
      seen.set(r.awayTeam, { teamName: r.awayTeam, rating: r.awayRatingAfter });
    }
  }
  return Array.from(seen.values());
}

function MovementIcon({ movement }: { movement: Movement | undefined }) {
  if (movement === "up") return <ArrowUp className="size-3.5 shrink-0 text-up" />;
  if (movement === "down") return <ArrowDown className="size-3.5 shrink-0 text-down" />;
  return <Minus className="size-3.5 shrink-0 text-muted-foreground/30" />;
}

export function StandingsBoard({ feed }: { feed: MatchResult[] }) {
  const sorted = deriveStandings(feed).sort((a, b) => b.rating - a.rating);

  // Tracks each team's rank across renders so a reorder can be shown as an
  // up/down arrow, not just inferred from the reorder animation alone.
  const prevRanksRef = useRef<Map<string, number>>(new Map());
  const [movement, setMovement] = useState<Map<string, Movement>>(new Map());

  useEffect(() => {
    const nextRanks = new Map(sorted.map((row, i) => [row.teamName, i]));
    const nextMovement = new Map<string, Movement>();

    nextRanks.forEach((rank, teamName) => {
      const prevRank = prevRanksRef.current.get(teamName);
      if (prevRank === undefined || prevRank === rank) nextMovement.set(teamName, "same");
      else nextMovement.set(teamName, rank < prevRank ? "up" : "down");
    });

    setMovement(nextMovement);
    prevRanksRef.current = nextRanks;
    // Only recompute when the feed actually grows by a new result.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [feed.length]);

  if (sorted.length === 0) {
    return <p className="py-10 text-center text-sm text-muted-foreground">No ratings yet.</p>;
  }

  return (
    <div className="space-y-1">
      <AnimatePresence initial={false}>
        {sorted.map((row, i) => (
          <motion.div
            key={row.teamName}
            layout
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ type: "spring", stiffness: 350, damping: 32 }}
            className="flex items-center justify-between gap-2 rounded-md border px-3 py-2 text-base"
          >
            <div className="flex min-w-0 items-center gap-2.5">
              <span className="w-5 text-right text-sm tabular-nums text-muted-foreground">
                {i + 1}
              </span>
              <MovementIcon movement={movement.get(row.teamName)} />
              <span className="truncate font-medium">{row.teamName}</span>
            </div>
            <span className="shrink-0 font-semibold tabular-nums">{Math.round(row.rating)}</span>
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  );
}
