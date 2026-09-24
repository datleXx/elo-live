import { useRef, useState } from "react";
import { toast } from "sonner";
import { CheckCircle2, CircleDashed, Loader2, XCircle } from "lucide-react";
import { ingest } from "@/lib/api";
import { seasonsFrom, seasonLabel } from "@/lib/seasons";
import { NATIONS } from "@/lib/divisions";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

type SeasonState = "pending" | "running" | "done" | "error";
/** statuses[divisionCode][season] */
type StatusMap = Record<string, Record<string, SeasonState>>;

export function BulkIngestPanel() {
  const [country, setCountry] = useState(NATIONS[0].country);
  const [running, setRunning] = useState(false);
  const [statuses, setStatuses] = useState<StatusMap>({});
  const [current, setCurrent] = useState<{ division: string; season: string } | null>(null);
  const cancelRef = useRef(false);

  const nation = NATIONS.find((n) => n.country === country)!;

  async function handleRunAll() {
    setRunning(true);
    cancelRef.current = false;

    const initial: StatusMap = {};
    for (const div of nation.divisions) {
      initial[div.code] = {};
      for (const s of seasonsFrom(div.earliestSeason)) initial[div.code][s] = "pending";
    }
    setStatuses(initial);

    let failedCount = 0;
    for (const div of nation.divisions) {
      if (cancelRef.current) break;
      for (const season of seasonsFrom(div.earliestSeason)) {
        if (cancelRef.current) break;
        setCurrent({ division: div.code, season });
        setStatuses((prev) => ({
          ...prev,
          [div.code]: { ...prev[div.code], [season]: "running" },
        }));
        try {
          await ingest(div.code, season);
          setStatuses((prev) => ({
            ...prev,
            [div.code]: { ...prev[div.code], [season]: "done" },
          }));
        } catch {
          failedCount++;
          setStatuses((prev) => ({
            ...prev,
            [div.code]: { ...prev[div.code], [season]: "error" },
          }));
        }
      }
    }

    setCurrent(null);
    setRunning(false);
    if (cancelRef.current) {
      toast.info("Bulk ingest cancelled");
    } else {
      toast.success(
        `Bulk ingest finished for ${country}${failedCount > 0 ? ` — ${failedCount} season(s) unavailable` : ""}`,
      );
    }
  }

  function handleCancel() {
    cancelRef.current = true;
  }

  const hasStatuses = Object.keys(statuses).length > 0;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Ingest full history</CardTitle>
        <CardDescription>
          Ingests every division and every season football-data.co.uk has for
          a nation, oldest first, one season at a time.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex flex-wrap items-end gap-3">
          <Select value={country} onValueChange={setCountry} disabled={running}>
            <SelectTrigger className="w-44">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {NATIONS.map((n) => (
                <SelectItem key={n.country} value={n.country}>
                  {n.country}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {!running ? (
            <Button onClick={handleRunAll}>
              Ingest all {nation.divisions.length} divisions
            </Button>
          ) : (
            <Button variant="outline" onClick={handleCancel}>
              Cancel
            </Button>
          )}
          {running && current && (
            <span className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <Loader2 className="size-3 animate-spin" />
              {current.division} — {seasonLabel(current.season)}
            </span>
          )}
        </div>

        {hasStatuses && (
          <div className="space-y-3">
            {nation.divisions.map((div) => {
              const seasonStatuses = statuses[div.code] ?? {};
              const seasons = Object.keys(seasonStatuses);
              const done = seasons.filter((s) => seasonStatuses[s] === "done").length;
              const failed = seasons.filter((s) => seasonStatuses[s] === "error").length;
              const processed = done + failed;

              return (
                <div key={div.code} className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span className="font-medium text-foreground">{div.name}</span>
                    <span>
                      {done} / {seasons.length} done
                      {failed > 0 && (
                        <span className="text-destructive"> · {failed} failed</span>
                      )}
                    </span>
                  </div>
                  <Progress value={(processed / (seasons.length || 1)) * 100} />
                  <div className="grid grid-cols-12 gap-1 sm:grid-cols-[repeat(16,minmax(0,1fr))] md:grid-cols-[repeat(20,minmax(0,1fr))]">
                    {seasons.map((s) => {
                      const state = seasonStatuses[s];
                      return (
                        <div
                          key={s}
                          title={`${div.name} ${seasonLabel(s)} — ${state}`}
                          className="flex items-center justify-center rounded-sm border p-0.5"
                        >
                          {state === "done" && (
                            <CheckCircle2 className="size-3 text-up" />
                          )}
                          {state === "error" && (
                            <XCircle className="size-3 text-destructive" />
                          )}
                          {state === "running" && (
                            <Loader2 className="size-3 animate-spin text-primary" />
                          )}
                          {state === "pending" && (
                            <CircleDashed className="size-3 text-muted-foreground/40" />
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
