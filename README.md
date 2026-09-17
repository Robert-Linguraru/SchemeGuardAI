# SchemeGuardAI

SchemeGuardAI manages card-scheme rules, qualifies imported transactions, and calculates interchange fees.

## Main features

- **Accounts:** registration, login, and profile updates.
- **Rules:** create, list, inspect, and delete card-scheme rules with conditions, priorities, effective dates, and interchange rates.
- **CSV imports:** upload transaction files, track import progress, and cancel uploads.
- **Qualification API:** evaluate pending transactions against rules, log decisions, and calculate fees.
- **Dashboard:** view transactions and summary statistics.

## Technology and structure

| Directory | Purpose | Technology |
| --- | --- | --- |
| `frontend/` | Web interface | React 18, TypeScript, Vite 6 |
| `backend/` | REST API, authentication, rules, and CSV imports | Java 26, Spring Boot 4.1.1, Maven |
| `ML/` | ML service scaffold | Python 3.12, FastAPI |
| `db/` | Database schema and reference data | PostgreSQL 17 |

## Run locally

Install Docker with Docker Compose, then run this command from the project root:

```powershell
Copy-Item .env.example .env
```

Open `.env` and fill the blank password and JWT-secret values. The `.env` file is ignored
by Git and must never be committed. Then start the application:

Generate a suitable JWT secret in PowerShell with:

```powershell
[Convert]::ToHexString(
    [Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
).ToLower()
```

```sh
docker compose up --build --remove-orphans
```

| Service | Local address |
| --- | --- |
| Web application | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| ML API documentation | http://localhost:8000/docs |
| ML health check | http://localhost:8000/health |
| Ollama API | http://localhost:11434 |

Open the web application and register an account to get started. The application uses the
Supabase PostgreSQL database configured in `.env`.

To stop the services while keeping database data:

```sh
docker compose down
```

### Supabase database setup

Docker Compose does not create or migrate the remote Supabase database. Before starting
the application for the first time, run these files once in the Supabase SQL Editor, in
the following order:

1. `country_regions.sql` creates the country mapping table.
2. `create_schema.sql` creates the main tables.
3. `rule_interpreter.sql` creates fallback rates and evaluation indexes.
4. `seed_reference_data.sql` inserts roles, card schemes, and 249 country mappings.
5. `upload_tables.sql` creates the upload tables.

The backend uses `spring.jpa.hibernate.ddl-auto=validate`, so startup fails with a clear
schema-validation error if these tables have not yet been created in Supabase. The
`--remove-orphans` option removes the old local PostgreSQL container after this migration;
it does not delete its `postgres_data` volume.

![DbDiagram](mermaid.png)

### Configuration

Backend defaults and upload limits are defined in [application.properties](backend/src/main/resources/application.properties).

| Environment variable | Purpose / default |
| --- | --- |
| `SPRING_DATASOURCE_URL` | Supabase JDBC URL; required |
| `SPRING_DATASOURCE_USERNAME` | Supabase database user; required |
| `SPRING_DATASOURCE_PASSWORD` | Supabase database password; required |
| `CORS_ALLOWED_ORIGIN` | Allowed frontend origin; `http://localhost:5173` |
| `APP_JWT_SECRET` | JWT signing secret of at least 32 characters; required |
| `UPLOAD_STORAGE_DIRECTORY` | Temporary upload storage; `/tmp/schemeguard-transaction-uploads` |
| `OLLAMA_URL` | Ollama base URL; Compose supplies `http://ollama:11434` |
| `OLLAMA_MODEL` | Model pulled and used by the backend; `qwen3:4b` |
| `OLLAMA_PORT` | Ollama port exposed on the host; `11434` |
| `OLLAMA_IMAGE` | Ollama Docker image; `ollama/ollama:latest` |

Spring imports `.env` from either the repository root or the `backend` module's parent
directory. This supports starting the backend from IntelliJ or Maven. Docker Compose also
reads the repository-level `.env` automatically and passes the same Supabase connection
values into the backend container. Both launch methods therefore use the same database.

For Supabase, use a JDBC URL without embedding the password, for example:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://your-pooler-host:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=your-supabase-database-user
SPRING_DATASOURCE_PASSWORD=your-supabase-database-password
```

## API overview

| Endpoint | Purpose |
| --- | --- |
| `POST /api/auth/register`, `POST /api/auth/login` | Register or sign in |
| `GET /api/auth/me`, `PUT /api/auth/profile` | Read or update the current account |
| `GET /api/rule/getRules`, `GET /api/rule/getRule?id=...` | List rules or read one rule |
| `POST /api/rule/upload`, `POST /api/rule/delete?id=...` | Create or delete a rule |
| `GET /api/transactions/mock` | Retrieve mock dashboard transactions |
| `POST /api/uploads` | Create an upload session |
| `PUT /api/uploads/{id}/parts/{partNumber}` | Upload a binary file chunk |
| `POST /api/uploads/{id}/complete` | Queue the CSV import |
| `GET /api/uploads/{id}`, `DELETE /api/uploads/{id}` | Check progress or cancel an upload |
| `POST /predict` (ML service) | Placeholder prediction endpoint |
| `POST /api/qualifications/evaluate` | Compute qualifications and fees |
| `GET /api/transactions/{id}/explanation` | Generate a Romanian explanation through Ollama |

Protected backend requests use `Authorization: Bearer <token>`. Current access rules are defined in [SecurityConfig.java](backend/src/main/java/org/schemeguard/backend/config/SecurityConfig.java).

For a transaction CSV example, see [transactions_40.csv](transactions_40.csv). Required columns are defined in [CsvImportService.java](backend/src/main/java/org/schemeguard/backend/service/csvUpload/CsvImportService.java).

# Rule-based component example
```json

{
  "scheme_id": "8c6d8f90-6b3f-4c7b-9b4a-111111111111",
  "rule_code": "MC_EU_CONSUMER_CREDIT_POS",
  "rule_name": "Mastercard EU Consumer Credit POS",
  "region": "EU",
  "priority": 10,
  "conditions": {
    "all": [
      {
        "field": "card_type",
        "operator": "equals",
        "value": "CREDIT"
      },
      {
        "field": "card_category",
        "operator": "equals",
        "value": "CONSUMER"
      },
      {
        "field": "channel",
        "operator": "equals",
        "value": "POS"
      },
      {
        "field": "three_ds_used",
        "operator": "equals",
        "value": true
      },
      {
        "field": "clearing_delay_days",
        "operator": "less_than_or_equal",
        "value": 1
      }
    ]
  },
  "result": {
    "qualification_category": "EU_CONSUMER_CREDIT_POS",
    "interchange_rate": 0.003,
    "fee_type": "PERCENTAGE"
  },
  "effective_from": "2026-01-01",
  "effective_to": null,
  "version": 1,
  "active": true
}

```


# Transaction example

```json

{
  "transaction_id": "TX-100045",
  "network": "VISA",
  "amount": 100.00,
  "currency": "EUR",

  "merchant_country": "RO",
  "merchant_id": "MER-123",
  "merchant_name": "Example Electronics",
  "mcc": "5732",

  "card_country": "FR",
  "card_type": "consumer_credit",
  "card_product": "traditional",
  "is_commercial_card": false,

  "channel": "ecommerce",
  "entry_mode": "card_not_present",
  "authentication": "3ds_authenticated",

  "authorization_timestamp": "2026-09-01T10:00:00Z",
  "clearing_timestamp": "2026-09-02T10:00:00Z",

  "authorization_code": "A78291",
  "settlement_status": "settled",

  "merchant_data_complete": true,
  "recurring": false,
  "refund": false
}

```

# Qualification result example
```json

Qualified
{
  "qualification_status": "qualified",
  "fee_category": "EU_CONSUMER_CREDIT_ECOMMERCE",
  "estimated_interchange_rate": 0.30,
  "estimated_interchange_amount": 0.30,
  "currency": "EUR",
  "confidence": 0.96,
  "reasons": [
    "Cardul este consumer credit",
    "Comerciantul și cardul sunt din regiunea EU",
    "Tranzacția este autentificată 3DS",
    "Clearing-ul s-a făcut în mai puțin de 2 zile"
  ],
  "warnings": []
}

Unqualified
---------------------------------------------------------

{
  "qualification_status": "not_qualified",
  "fee_category": "FALLBACK_ECOMMERCE",
  "estimated_interchange_rate": 1.80,
  "estimated_interchange_amount": 1.80,
  "confidence": 0.91,
  "reasons": [
    "Clearing-ul a avut loc după 5 zile",
    "Datele comerciantului sunt incomplete"
  ],
  "recommendations": [
    "Trimiteți date complete ale comerciantului",
    "Reduceți intervalul dintre autorizare și clearing"
  ]
}

```
