import { IngestPanel } from "@/components/admin/IngestPanel";
import { BulkIngestPanel } from "@/components/admin/BulkIngestPanel";

export function AdminPage() {
  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-4">
      <IngestPanel />
      <BulkIngestPanel />
    </div>
  );
}
