# SchemeGuardAI

SchemeGuardAI is a application for managing card-scheme rules, with planned support for transaction qualification and interchange fee analysis.

## Main features

- **Accounts:** registration, login, and profile updates.
- **Rules:** create, list, inspect, and delete card-scheme rules with conditions, priorities, effective dates, and interchange rates.
- **CSV imports:** upload transaction files, track import progress, and cancel uploads.
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

```sh
docker compose up --build
```

| Service | Local address |
| --- | --- |
| Web application | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| ML API documentation | http://localhost:8000/docs |
| ML health check | http://localhost:8000/health |
| PostgreSQL | `localhost:5433` |

Open the web application and register an account to get started. The database seeds the `ADMIN`, `ANALYST`, and `MERCHANT` roles, plus Visa and Mastercard schemes.

To stop the services while keeping database data:

```sh
docker compose down
```

### Database setup

On the first startup with an empty database volume, PostgreSQL runs the scripts in `db/` in filename order:

1. `create_schema.sql` creates the main tables.
2. `seed_reference_data.sql` inserts roles and card schemes.
3. `upload_tables.sql` creates the upload tables.

### Configuration

Backend defaults and upload limits are defined in [application.properties](backend/src/main/resources/application.properties).

| Environment variable | Purpose / default |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/schemeguard` |
| `SPRING_DATASOURCE_USERNAME` | Database user; `schemeguard` |
| `SPRING_DATASOURCE_PASSWORD` | Database password; `schemeguard` |
| `CORS_ALLOWED_ORIGIN` | Allowed frontend origin; `http://localhost:5173` |
| `APP_JWT_SECRET` | Overrides the development JWT signing secret |
| `UPLOAD_STORAGE_DIRECTORY` | Temporary upload storage; `/tmp/schemeguard-transaction-uploads` |

## API overview

### Authentication and profile

| Endpoint | Purpose |
| --- | --- |
| `POST /api/auth/register`, `POST /api/auth/login` | Register or sign in |
| `GET /api/auth/me`, `PUT /api/auth/profile` | Read or update the current account |

### Card schemes

| Endpoint | Purpose |
| --- | --- |
| `GET /api/card-schemes` | List active schemes; supports `includeInactive`, `search`, `page`, `size` and `sort` |
| `GET /api/card-schemes/{id}` | Read one active scheme; admins can use `includeInactive=true` |
| `POST /api/card-schemes` | Create a card scheme |
| `PATCH /api/card-schemes/{id}` | Update the scheme `code` and `name` |
| `PATCH /api/card-schemes/{id}/activate` | Activate the scheme; `{ "cascade": true }` also activates associated entities |
| `PATCH /api/card-schemes/{id}/deactivate` | Deactivate the scheme and associated rules, transactions and results |
| `DELETE /api/card-schemes/{id}` | Hard-delete the scheme and its dependent rules, transactions and results |

### Rules

| Endpoint | Purpose |
| --- | --- |
| `GET /api/rule/getRules`, `GET /api/rule/getRule?id=...` | List rules or read one rule |
| `POST /api/rule/upload`, `POST /api/rule/delete?id=...` | Create or delete a rule |

### Transactions and uploads

| Endpoint | Purpose |
| --- | --- |
| `GET /api/transactions/mock` | Retrieve mock dashboard transactions |
| `POST /api/uploads` | Create an upload session |
| `PUT /api/uploads/{id}/parts/{partNumber}` | Upload a binary file chunk |
| `POST /api/uploads/{id}/complete` | Queue the CSV import |
| `GET /api/uploads/{id}`, `DELETE /api/uploads/{id}` | Check progress or cancel an upload |

### Machine learning

| Endpoint | Purpose |
| --- | --- |
| `POST /predict` (ML service) | Placeholder prediction endpoint |

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
