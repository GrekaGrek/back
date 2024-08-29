TRUNCATE TABLE conversion_fees RESTART IDENTITY CASCADE;

INSERT INTO conversion_fees (id, from_currency, to_currency, fee)
VALUES (4, 'CHF', 'PLN', 0.05),
       (2, 'HUF', 'JPY', 0.08),
       (3, 'ISK', 'NOK', 0.02);