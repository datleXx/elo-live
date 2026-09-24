/** Newest season football-data.co.uk has, confirmed by hand. Update as new seasons appear. */
const NEWEST_START_YEAR = 2026;
const OLDEST_START_YEAR = 1993;

function startYearOf(code: string): number {
  const twoDigit = Number(code.slice(0, 2));
  return (twoDigit >= 93 ? 1900 : 2000) + twoDigit;
}

function seasonCode(startYear: number): string {
  const start = String(startYear % 100).padStart(2, "0");
  const end = String((startYear + 1) % 100).padStart(2, "0");
  return `${start}${end}`;
}

/** All season codes from 1993/94 to newest, e.g. ["9394", "9495", ..., "2627"]. */
export const ALL_SEASONS: string[] = Array.from(
  { length: NEWEST_START_YEAR - OLDEST_START_YEAR + 1 },
  (_, i) => seasonCode(OLDEST_START_YEAR + i),
);

/** Season codes from a given earliest season (inclusive) to newest. */
export function seasonsFrom(earliestSeason: string): string[] {
  const earliestYear = startYearOf(earliestSeason);
  return ALL_SEASONS.filter((s) => startYearOf(s) >= earliestYear);
}

export function seasonLabel(code: string): string {
  const startYear = startYearOf(code);
  return `${startYear}/${code.slice(2, 4)}`;
}
