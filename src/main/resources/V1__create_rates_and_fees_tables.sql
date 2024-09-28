CREATE TABLE conversion_fees (
    id              SERIAL PRIMARY KEY NOT NULL,
    from_currency   VARCHAR(3) NOT NULL,
    to_currency     VARCHAR(3) NOT NULL,
    fee             DECIMAL(19, 4)

    CONSTRAINT chk_currency_length_from CHECK (LENGTH(from_currency) = 3),
    CONSTRAINT chk_currency_length_to CHECK (LENGTH(to_currency) = 3),
    CONSTRAINT unique_currency_pair UNIQUE (from_currency, to_currency)
);

CREATE TABLE exchange_rates (
    currency VARCHAR(3) NOT NULL,
    rate     DECIMAL(19, 4) NOT NULL

    CONSTRAINT chk_currency_length CHECK (LENGTH(currency) = 3)
);