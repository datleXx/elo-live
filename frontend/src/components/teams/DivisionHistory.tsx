import { divisionLabel } from "@/lib/divisions";
import { Badge } from "@/components/ui/badge";
import type { TeamDivisionSpell } from "@/types";

export function DivisionHistory({ spells }: { spells: TeamDivisionSpell[] }) {
  if (spells.length === 0) return null;

  return (
    <div className="space-y-2">
      {spells.map((s) => (
        <div
          key={s.competition}
          className="flex items-center justify-between rounded-md border px-3 py-2 text-sm"
        >
          <div className="flex items-center gap-2">
            <Badge variant="outline">{s.competition}</Badge>
            <span className="font-medium">{divisionLabel(s.competition)}</span>
          </div>
          <span className="text-muted-foreground">
            {s.firstMatch} → {s.lastMatch}
          </span>
        </div>
      ))}
    </div>
  );
}
