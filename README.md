# Exemplu rule-based component (regula pentru categorie interchange fee tranzactie)
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