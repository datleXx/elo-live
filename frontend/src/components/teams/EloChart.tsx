import { CartesianGrid, Line, LineChart, XAxis, YAxis } from "recharts";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart";
import type { RatingUpdate } from "@/types";

const chartConfig = {
  rating: {
    label: "Elo rating",
    color: "var(--primary)",
  },
} satisfies ChartConfig;

export function EloChart({ history }: { history: RatingUpdate[] }) {
  const data = history.map((h) => ({
    date: h.matchDate,
    rating: Math.round(h.rating),
  }));

  return (
    <ChartContainer config={chartConfig} className="h-80 w-full">
      <LineChart data={data} margin={{ left: 12, right: 12, top: 8, bottom: 8 }}>
        <CartesianGrid vertical={false} />
        <XAxis
          dataKey="date"
          tickLine={false}
          axisLine={false}
          tickMargin={8}
          minTickGap={32}
        />
        <YAxis
          domain={["dataMin - 30", "dataMax + 30"]}
          tickLine={false}
          axisLine={false}
          tickMargin={8}
          width={40}
        />
        <ChartTooltip content={<ChartTooltipContent />} />
        <Line
          dataKey="rating"
          type="monotone"
          stroke="var(--color-rating)"
          strokeWidth={2}
          dot={false}
        />
      </LineChart>
    </ChartContainer>
  );
}
