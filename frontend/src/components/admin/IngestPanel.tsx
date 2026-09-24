import { useState } from "react";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { ingest, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { DivisionSelect } from "@/components/DivisionSelect";

export function IngestPanel() {
  const [division, setDivision] = useState("E0");
  const [season, setSeason] = useState("2324");
  const [loading, setLoading] = useState(false);

  async function handleIngest() {
    setLoading(true);
    try {
      const message = await ingest(division, season);
      toast.success(message);
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Request failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Ingest a season</CardTitle>
        <CardDescription>
          Pulls one season's results directly from football-data.co.uk.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex flex-wrap items-end gap-3">
          <div className="flex flex-col gap-1.5">
            <Label>Division</Label>
            <DivisionSelect value={division} onValueChange={setDivision} />
          </div>
          <div className="flex flex-col gap-1.5">
            <Label>Season</Label>
            <Input
              className="w-28"
              value={season}
              onChange={(e) => setSeason(e.target.value)}
              placeholder="2324"
            />
          </div>
          <Button onClick={handleIngest} disabled={loading}>
            {loading && <Loader2 className="size-4 animate-spin" />}
            Ingest
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
