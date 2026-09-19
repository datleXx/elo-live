ALTER TABLE matches
    ALTER COLUMN full_time_home_goals DROP NOT NULL,
    ALTER COLUMN full_time_away_goals DROP NOT NULL,
    ALTER COLUMN full_time_result DROP NOT NULL;