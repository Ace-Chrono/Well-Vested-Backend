# Persisted insights

Uses controller -> service interface -> service implementation -> JPA repository,
with entity-to-response conversion through a Spring MapStruct mapper. Insight rows
are stored in the `insights` table using the existing datasource and schema settings.
No AI or separate Plaid product is required.

## Endpoints

Read the latest saved snapshot (initially an empty array):

- GET `/api/insights/spending?currency=USD`
- GET `/api/insights/investments?currency=USD`

Sync source records from the connected Plaid Item, calculate and persist insights:

- POST `/api/insights/spending/sync?startDate=2026-09-01&endDate=2026-09-30&currency=USD`
- POST `/api/insights/investments/sync?currency=USD`

To calculate using already persisted records without calling Plaid, replace `/sync`
with `/generate`. POST responses contain the saved insight DTOs. Use the existing
Plaid token creation/exchange flow first. PlaidConfig currently uses Sandbox.

Each refresh replaces the previous snapshot for that scope and currency. Spending
retains the latest requested date range, rather than an archive of every range.
GET does not sync or recalculate; generatedAt identifies the snapshot's generation
time, not the institution's data freshness. Regenerate after source CRUD or sync.
Replacing the connected Plaid Item clears saved insights.

Refreshes run in a database transaction. Sync failures do not replace existing
insights. The application retains its current single-user, first-connected-Item model.

## Rules and response values

Spending includes finite positive, posted transaction amounts in the requested
currency and inclusive date range. Excludes pending entries, inflows/refunds,
known transfers, income and loan payments. This is gross recorded spending, not
net spending after refunds. Unknown categories are UNCATEGORIZED and can include
unidentified transfers. Currency-less records are excluded, not assumed to be USD.
Comparisons use the immediately preceding period with the same number of days.
A zero prior total produces a null change percentage. Missing history is not proof
of zero real spending. Dates must be ordered and span at most 366 days.

Investment value uses shares * institution price for finite positive quantities
and nonnegative prices in the requested currency. Cash is in the denominator but
is not flagged as security concentration. Same-symbol positions across accounts
are combined; positions lacking tickers use security ID, then internal record ID.
A holding at or above 40% produces HOLDING_CONCENTRATION. Crypto at or above 20%
produces a warning. These are application heuristics, not investment advice or a
comprehensive risk score. ETFs are treated as positions; no underlying exposures
or historical volatility are available. Short positions and unsupported/missing
valuation data are excluded and reported by DATA_COVERAGE.

DTO fields: insightId, scope, type, severity, subject, message, isoCurrencyCode,
startDate, endDate, value, previousValue, percentage, generatedAt. Percentages are
percentage points (50 means 50%). value/previousValue are currency amounts except
DATA_COVERAGE, whose value counts excluded holdings. NO_DATA has null metrics.

Transaction sync now retains isoCurrencyCode and personalFinanceCategory. Old
transaction rows missing those fields need re-importing (existing token exchange
resets the transaction cursor and imported data); cursor sync alone only updates
records Plaid reports as added/modified.

References:
- https://plaid.com/docs/api/products/transactions/
- https://plaid.com/docs/api/products/investments/
