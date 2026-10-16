# Investments API

Uses the same controller, DTO, service, MapStruct mapper and Spring Data JPA
repository layers as transactions. Investment records use the configured application
database and generated internal UUIDs. No local mock seeder is used.

## Plaid Sandbox flow

PlaidConfig currently points to Sandbox. Requests use Plaid's API and Sandbox
fixtures rather than live brokerage data.

1. POST `/api/test/plaid/public-token` to create a Sandbox Item with Transactions
   and Investments enabled, then exchange its public token through the existing
   Plaid token-exchange endpoint.
2. POST `/api/investments/sync` to fetch `/investments/holdings/get` and persist holdings.
3. GET `/api/investments` to read the stored portfolio.

Link requests keep Transactions required and request Investments as an optional
product for institutions that support it. Previously created Sandbox Items may
need to be recreated with Investments enabled.

Holdings and securities are joined by security ID. Each position is identified
by Plaid Item, account, and security, so the same security in two accounts remains
two positions. Sync preserves internal UUIDs, updates existing positions, and removes
positions missing from the successful full snapshot. Manual records are preserved.
Database updates are transactional; a failed Plaid call leaves existing holdings
untouched. Replacing the connected Item clears imported holdings just as it clears
imported transactions. Like transaction sync, this uses the first connected Item.

The response includes accountId, securityId and isoCurrencyCode. Price comes from
institution_price and shares from quantity. Tickers can be null. dayChange is null
for imported holdings because this endpoint does not supply daily percentage changes.
Asset types include STOCK, CRYPTO, ETF, MUTUAL_FUND, BOND, CASH, OPTION and OTHER.

Existing CRUD and filter routes remain available. Update DTO mapping follows the
transaction convention of copying null fields. Imported identity fields cannot be
overwritten by manual update DTOs. Schema updates use the same Hibernate/database
schema management as transactions.

The frontend still uses its existing local mock data. Charts and daily performance
need additional data and frontend integration.

Plaid reference: https://plaid.com/docs/api/products/investments/
