ALTER TABLE matches
    ADD CONSTRAINT uq_matches_natural_key UNIQUE (competition, match_date, home_team_id, away_team_id);