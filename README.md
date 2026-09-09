# Exemplu rule-based component (regula pentru categorie interchange fee tranzactie)
```json

{
  "rule_id": "MC_EU_CONSUMER_CREDIT_POS",
  "network": "MASTERCARD",
  "region": "EU",
  "conditions": {
    "card_type": "consumer_credit",
    "channel": "pos",
    "merchant_data_complete": true,
    "clearing_delay_max_days": 1
  },
  "result": {
    "status": "qualified",
    "rate": 0.0030,
    "fee_type": "percentage"
  },
  "priority": 10,
  "effective_from": "2026-01-01"
}

```


# Exemplu tranzactie

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

# Exemplu rezultate tranzactie dupa aplicarea regulilor

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