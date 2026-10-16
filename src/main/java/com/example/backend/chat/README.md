# Financial chat

POST `/api/chat`:

```json
{
  "message": "Explain my spending and portfolio concentration.",
  "currency": "USD",
  "startDate": "2026-09-01",
  "endDate": "2026-09-30"
}
```

Returns turnId, conversationId, message, reply, currency, startDate, endDate and
createdAt. Send conversationId in later requests to continue. GET
`/api/chat/{conversationId}` returns the latest six successful turns chronologically.
Each turn is saved through JPA in chat_turns using the existing datasource/schema
configuration. A failed OpenAI request creates no turn. No database transaction is
held across the network call.

## Setup

Set OPENAI_API_KEY on the backend process, then restart. It is never sent to the
frontend. Optionally set OPENAI_MODEL (default gpt-5.6-terra); choose a Responses API
model available to your account. Alternatively use openai.api-key / openai.model
in your private Spring configuration. Do not commit keys. Missing credentials return
503 for chat without preventing other backend features from starting.

Uses POST https://api.openai.com/v1/responses over Java's HTTP client. No new
SDK/framework dependencies, embeddings, vector database or agent tools. OpenAI
response storage is disabled with store=false; this is not a zero-retention guarantee.
Chat requests send selected financial records and recent conversation text to OpenAI.

## Context and limitations

Context is explicitly projected from persisted insights, transactions and holdings.
It excludes access tokens, account IDs and Plaid identifiers. No automatic Plaid sync
or insight generation occurs. Sync/generate using the existing endpoints first.
Defaults: USD and the last 30 calendar days in America/New_York. Explicit dates are
inclusive and may span at most 366 days. Message length is limited to 4000 characters.

SQL queries bound context to 50 latest insights in the currency, 100 latest dated
transactions in the currency/range and 100 holdings in that currency. Insight periods
and generation times are included; they may differ from the requested date range.
Raw samples are explicitly labelled incomplete and must not be used as complete
portfolio/spending totals. Text fields are capped at 500 characters. Null data and
nonfinite numbers are omitted. Unknown currency records are excluded.

This is the application's existing single-user demo model, without authentication
or per-user ownership. Conversation IDs are not authorization. Before multi-user
hosting, scope all data and conversation queries to the authenticated user. Start a
new conversation after replacing bank access; previous chat replies are historical.
Only the latest six turns are sent, though all successful turns remain in the database.
Concurrent messages can share the same prior history; send messages sequentially.

There is a 10-second connect timeout and 45-second request timeout. Rate/quota errors
return 503, timeouts 504, and other upstream/invalid replies 502. No raw provider
errors or financial prompts are logged. Frontend integration is not included.

Official documentation: https://developers.openai.com/api/reference/java/resources/responses/methods/create
