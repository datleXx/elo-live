CREATE SEQUENCE teams_id_seq START WITH 1
    INCREMENT BY 50;

CREATE TABLE teams
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE SEQUENCE matches_id_seq START WITH 1
    INCREMENT BY 50;

CREATE TABLE matches
(
    id                   BIGINT PRIMARY KEY,
    competition          VARCHAR    NOT NULL,
    match_date           DATE       NOT NULL,
    home_team_id         BIGINT     NOT NULL REFERENCES teams (id),
    away_team_id         BIGINT     NOT NULL REFERENCES teams (id),
    full_time_home_goals INT        NOT NULL,
    full_time_away_goals INT        NOT NULL,
    full_time_result     VARCHAR(1) NOT NULL,
    home_odds            NUMERIC,
    draw_odds            NUMERIC,
    away_odds            NUMERIC
);

CREATE INDEX idx_matches_competition_date ON matches (competition, match_date);

CREATE SEQUENCE ratings_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE predictions_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE ratings
(
    id             BIGINT PRIMARY KEY,
    team_id        BIGINT  NOT NULL REFERENCES teams (id),
    rating         NUMERIC NOT NULL,
    as_of_match_id BIGINT REFERENCES matches (id)
);

CREATE TABLE predictions
(
    id                 BIGINT PRIMARY KEY,
    match_id           BIGINT  NOT NULL REFERENCES matches (id),
    home_rating_before NUMERIC NOT NULL,
    away_rating_before NUMERIC NOT NULL,
    home_win_prob      NUMERIC NOT NULL,
    draw_prob          NUMERIC NOT NULL,
    away_win_prob      NUMERIC NOT NULL
);