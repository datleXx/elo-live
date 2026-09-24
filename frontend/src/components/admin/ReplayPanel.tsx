import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { startReplay, stopReplay, replayStatus, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { DivisionSelect } from "@/components/DivisionSelect";

interface Props {
  /** Called with the division as soon as a replay actually starts, so a
   * parent page can point its live view at `{division}_REPLAY` automatically. */
  onStart?: (division: string) => void;
}

export function ReplayPanel({ onStart }: Props) {
  const [division, setDivision] = useState("E0");
  const [season, setSeason] = useState("2324");
  const [delayMillis, setDelayMillis] = useState(2000);
  const [running, setRunning] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const poll = async () => {
      try {
        const isRunning = await replayStatus();
        if (!cancelled) setRunning(isRunning);
      } catch {
        // transient — next poll will retry
      }
    };
    poll();
    const id = setInterval(poll, 2000);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, []);

  async function handleStart() {
    setLoading(true);
    try {
      const text = await startReplay(division, season, delayMillis);
      toast.success(text);
      setRunning(true);
      onStart?.(division);
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Request failed");
    } finally {
      setLoading(false);
    }
  }

  async function handleStop() {
    try {
      await stopReplay();
      toast.success("Stopped");
      setRunning(false);
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Request failed");
    }
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between">
        <div>
          <CardTitle>Replay a season</CardTitle>
          <CardDescription>
            Streams an old season through the live pipeline, one matchday at
            a time.
          </CardDescription>
        </div>
        <Badge variant={running ? "default" : "secondary"} className="gap-1.5">
          <span
            className={`size-1.5 rounded-full ${running ? "bg-up" : "bg-muted-foreground"}`}
          />
          {running ? "running" : "idle"}
        </Badge>
      </CardHeader>
      <CardContent>
        <div className="flex flex-wrap items-end gap-3">
          <div className="flex flex-col gap-1.5">
            <Label>Division</Label>
            <DivisionSelect value={division} onValueChange={setDivision} disabled={running} />
          </div>
          <div className="flex flex-col gap-1.5">
            <Label>Season</Label>
            <Input
              className="w-28"
              value={season}
              onChange={(e) => setSeason(e.target.value)}
              placeholder="2324"
              disabled={running}
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <Label>Delay (ms)</Label>
            <Input
              type="number"
              className="w-28"
              value={delayMillis}
              onChange={(e) => setDelayMillis(Number(e.target.value))}
              min={1}
              disabled={running}
            />
          </div>
          <Button onClick={handleStart} disabled={running || loading}>
            {loading && <Loader2 className="size-4 animate-spin" />}
            Start
          </Button>
          <Button variant="outline" onClick={handleStop} disabled={!running}>
            Stop
          </Button>
        </div>

        <p className="mt-4 text-xs text-muted-foreground">
          Pushes live updates to{" "}
          <code className="rounded bg-muted px-1 py-0.5">{division}_REPLAY</code> — the
          panels below switch to it automatically once this starts.
        </p>
      </CardContent>
    </Card>
  );
}
