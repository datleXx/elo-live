export interface RatingUpdate {
  competition: string;
  teamId: number;
  teamName: string;
  rating: number;
  matchDate: string;
}

/** One WebSocket push = one finished match, pushed to /topic/ratings/{competition}. */
export interface MatchResult {
  competition: string;
  matchDate: string;
  homeTeam: string;
  awayTeam: string;
  homeGoals: number;
  awayGoals: number;
  homeRatingBefore: number;
  awayRatingBefore: number;
  homeRatingAfter: number;
  awayRatingAfter: number;
}

export interface Team {
  id: number;
  name: string;
}

export interface TeamDivisionSpell {
  competition: string;
  firstMatch: string;
  lastMatch: string;
}

export interface TeamCompetition {
  teamId: number;
  competition: string;
}

/** One WebSocket push = one upcoming fixture predicted, pushed to /topic/predictions/{competition}. */
export interface LivePrediction {
  competition: string;
  matchDate: string;
  homeTeam: string;
  awayTeam: string;
  homeWinProb: number;
  drawProb: number;
  awayWinProb: number;
  marketHomeWinProb: number | null;
  marketDrawProb: number | null;
  marketAwayWinProb: number | null;
}
