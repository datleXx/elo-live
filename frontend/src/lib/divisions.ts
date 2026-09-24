export interface Division {
  code: string;
  name: string;
  /** Earliest season football-data.co.uk actually has for this division, verified by hand. */
  earliestSeason: string;
}

export interface Nation {
  country: string;
  divisions: Division[];
}

export const NATIONS: Nation[] = [
  {
    country: "England",
    divisions: [
      { code: "E0", name: "Premier League", earliestSeason: "9394" },
      { code: "E1", name: "Championship", earliestSeason: "9394" },
      { code: "E2", name: "League One", earliestSeason: "9394" },
      { code: "E3", name: "League Two", earliestSeason: "9394" },
      { code: "EC", name: "National League", earliestSeason: "0506" },
    ],
  },
  {
    country: "Spain",
    divisions: [
      { code: "SP1", name: "La Liga", earliestSeason: "9394" },
      { code: "SP2", name: "Segunda División", earliestSeason: "9394" },
    ],
  },
  {
    country: "Germany",
    divisions: [
      { code: "D1", name: "Bundesliga", earliestSeason: "9394" },
      { code: "D2", name: "2. Bundesliga", earliestSeason: "9394" },
    ],
  },
  {
    country: "Italy",
    divisions: [
      { code: "I1", name: "Serie A", earliestSeason: "9394" },
      { code: "I2", name: "Serie B", earliestSeason: "9798" },
    ],
  },
  {
    country: "France",
    divisions: [
      { code: "F1", name: "Ligue 1", earliestSeason: "9394" },
      { code: "F2", name: "Ligue 2", earliestSeason: "9697" },
    ],
  },
];

/** Every division across every nation, flattened — for single-competition pickers. */
export const ALL_DIVISIONS: Division[] = NATIONS.flatMap((n) => n.divisions);

export function divisionLabel(code: string): string {
  const div = ALL_DIVISIONS.find((d) => d.code === code);
  return div ? div.name : code;
}

export function countryOf(competition: string): string | undefined {
  return NATIONS.find((n) => n.divisions.some((d) => d.code === competition))?.country;
}
