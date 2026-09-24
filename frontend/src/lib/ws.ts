import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type { LivePrediction, MatchResult } from "@/types";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const MAX_FEED_LENGTH = 200;

export type Outcome = "H" | "D" | "A";

export interface ResolvedResult extends MatchResult {
  /** What was predicted for this fixture before the result arrived, if anything was. */
  predictedOutcome: Outcome | null;
  predictionCorrect: boolean | null;
}

export interface Round {
  matchDate: string;
  /** 1-based, in chronological order - Round 1 is the earliest matchday seen. */
  roundNumber: number;
  results: ResolvedResult[];
}

function outcomeOf(homeProb: number, drawProb: number, awayProb: number): Outcome {
  if (homeProb >= drawProb && homeProb >= awayProb) return "H";
  if (awayProb >= drawProb) return "A";
  return "D";
}

function actualOutcome(homeGoals: number, awayGoals: number): Outcome {
  if (homeGoals > awayGoals) return "H";
  if (awayGoals > homeGoals) return "A";
  return "D";
}

function fixtureKey(homeTeam: string, awayTeam: string): string {
  return `${homeTeam}|${awayTeam}`;
}

/**
 * The single live connection for a competition - one result feed (for
 * standings), grouped into rounds by matchDate (one replay tick = one
 * round), each result resolved against whatever prediction existed for
 * that exact fixture before the result arrived. A prediction is matched by
 * team names, the one thing both messages share, and is removed from
 * "pending" the moment its result resolves it.
 */
export function useLiveRounds(competition: string) {
  const [feed, setFeed] = useState<MatchResult[]>([]);
  const [rounds, setRounds] = useState<Map<string, ResolvedResult[]>>(new Map());
  const [pendingPredictions, setPendingPredictions] = useState<LivePrediction[]>([]);
  const [connected, setConnected] = useState(false);
  const predictionsRef = useRef<Map<string, LivePrediction>>(new Map());

  useEffect(() => {
    setFeed([]);
    setRounds(new Map());
    setPendingPredictions([]);
    predictionsRef.current = new Map();

    const client = new Client({
      webSocketFactory: () => new SockJS(`${BASE_URL}/ws`) as unknown as WebSocket,
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      setConnected(true);

      client.subscribe(`/topic/predictions/${competition}`, (message) => {
        const prediction: LivePrediction = JSON.parse(message.body);
        predictionsRef.current.set(fixtureKey(prediction.homeTeam, prediction.awayTeam), prediction);
        setPendingPredictions(Array.from(predictionsRef.current.values()));
      });

      client.subscribe(`/topic/ratings/${competition}`, (message) => {
        const result: MatchResult = JSON.parse(message.body);
        setFeed((prev) => [result, ...prev].slice(0, MAX_FEED_LENGTH));

        const key = fixtureKey(result.homeTeam, result.awayTeam);
        const prediction = predictionsRef.current.get(key);
        let predictedOutcome: Outcome | null = null;
        let predictionCorrect: boolean | null = null;

        if (prediction) {
          predictedOutcome = outcomeOf(
            prediction.homeWinProb,
            prediction.drawProb,
            prediction.awayWinProb,
          );
          predictionCorrect =
            predictedOutcome === actualOutcome(result.homeGoals, result.awayGoals);
          predictionsRef.current.delete(key);
          setPendingPredictions(Array.from(predictionsRef.current.values()));
        }

        setRounds((prev) => {
          const next = new Map(prev);
          const list = next.get(result.matchDate) ?? [];
          next.set(result.matchDate, [...list, { ...result, predictedOutcome, predictionCorrect }]);
          return next;
        });
      });
    };
    client.onWebSocketClose = () => setConnected(false);

    client.activate();
    return () => {
      client.deactivate();
    };
  }, [competition]);

  const chronologicalDates = Array.from(rounds.keys()).sort();
  const roundList: Round[] = chronologicalDates
    .map((matchDate, i) => ({
      matchDate,
      roundNumber: i + 1,
      results: rounds.get(matchDate)!,
    }))
    .reverse(); // newest round first, matching the feed convention

  const sortedPredictions = [...pendingPredictions].sort((a, b) =>
    a.matchDate.localeCompare(b.matchDate),
  );

  return { feed, rounds: roundList, pendingPredictions: sortedPredictions, connected };
}
