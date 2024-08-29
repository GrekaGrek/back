CREATE TABLE conversion_fees (
    id              SERIAL PRIMARY KEY NOT NULL,
    from_currency   VARCHAR(3) NOT NULL,
    to_currency     VARCHAR(3) NOT NULL,
    fee             DECIMAL(19, 4)
);

CREATE TABLE exchange_rates (
    currency VARCHAR(3) NOT NULL,
    rate     DECIMAL(19, 4) NOT NULL
);

-- Optional optimization for queries on currency pairs
CREATE INDEX idx_conversion_fee_from_currency ON conversion_fees (from_currency);
CREATE INDEX idx_conversion_fee_to_currency ON conversion_fees (to_currency);