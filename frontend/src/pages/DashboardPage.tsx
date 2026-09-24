import { useState } from "react";
import { PanelRight } from "lucide-react";
import { useLiveRounds } from "@/lib/ws";
import { RoundsFeed } from "@/components/dashboard/RoundsFeed";
import { PredictionsPanel } from "@/components/dashboard/PredictionsPanel";
import { StandingsBoard } from "@/components/dashboard/StandingsBoard";
import { ReplayPanel } from "@/components/admin/ReplayPanel";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";

export function DashboardPage() {
  const [competition, setCompetition] = useState("E0");
  const { feed, rounds, pendingPredictions, connected } = useLiveRounds(competition);

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6">
      <ReplayPanel onStart={(division) => setCompetition(`${division}_REPLAY`)} />

      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold tracking-tight text-foreground">Live view</h2>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <Label htmlFor="watching" className="text-xs text-muted-foreground">
              Watching
            </Label>
            <Input
              id="watching"
              className="h-8 w-40"
              value={competition}
              onChange={(e) => setCompetition(e.target.value)}
            />
          </div>
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="outline" size="sm" className="gap-1.5">
                <PanelRight className="size-4" />
                Standings
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-full gap-0 sm:max-w-md">
              <SheetHeader className="border-b">
                <SheetTitle>Standings</SheetTitle>
              </SheetHeader>
              <div className="flex-1 overflow-y-auto px-4 py-3">
                <StandingsBoard feed={feed} />
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <RoundsFeed rounds={rounds} connected={connected} />
        <PredictionsPanel predictions={pendingPredictions} connected={connected} />
      </div>
    </div>
  );
}
