ALTER TABLE predictions
    ADD COLUMN market_home_win_prob NUMERIC,
    ADD COLUMN market_draw_prob     NUMERIC,
    ADD COLUMN market_away_win_prob NUMERIC;