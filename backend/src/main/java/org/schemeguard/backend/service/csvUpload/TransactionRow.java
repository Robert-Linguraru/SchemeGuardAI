package org.schemeguard.backend.service.csvUpload;

import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public record TransactionRow(
        String transactionId,
        String network,
        BigDecimal amount,
        String currency,
        String merchantCountry,
        String merchantId,
        String merchantName,
        String mcc,
        String cardCountry,
        String cardType,
        String cardProduct,
        boolean commercialCard,
        String channel,
        String entryMode,
        String authentication,
        OffsetDateTime authorizationTimestamp,
        OffsetDateTime clearingTimestamp,
        String authorizationCode,
        String settlementStatus,
        boolean merchantDataComplete,
        boolean recurring,
        boolean refund,
        boolean cvvPresent,
        Map<String, String> rawData
) {

    public static TransactionRow from(CSVRecord record, long rowNumber) {
        String transactionId = required(record, "transaction_id", rowNumber);
        if (transactionId.length() > 100) {
            throw invalid(rowNumber, "transaction_id must contain at most 100 characters");
        }

        String network = required(record, "network", rowNumber)
                .toUpperCase(Locale.ROOT);
        if (network.length() > 30) {
            throw invalid(rowNumber, "network must contain at most 30 characters");
        }

        BigDecimal amount = decimal(record, "amount", rowNumber);
        if (amount.signum() <= 0) {
            throw invalid(rowNumber, "amount must be greater than zero");
        }

        String currency = countryOrCurrency(record, "currency", 3, rowNumber);
        String merchantCountry = countryOrCurrency(
                record,
                "merchant_country",
                2,
                rowNumber
        );
        String merchantId = required(record, "merchant_id", rowNumber);
        String merchantName = required(record, "merchant_name", rowNumber);
        if (merchantName.length() > 200) {
            throw invalid(rowNumber, "merchant_name must contain at most 200 characters");
        }

        String mcc = required(record, "mcc", rowNumber);
        if (!mcc.matches("[0-9]{4}")) {
            throw invalid(rowNumber, "mcc must contain exactly four digits");
        }

        String cardCountry = countryOrCurrency(record, "card_country", 2, rowNumber);
        String cardType = required(record, "card_type", rowNumber);
        String cardProduct = required(record, "card_product", rowNumber);
        boolean commercialCard = booleanValue(
                record,
                "is_commercial_card",
                rowNumber
        );
        String channel = required(record, "channel", rowNumber);
        String entryMode = required(record, "entry_mode", rowNumber);
        String authentication = required(record, "authentication", rowNumber);
        OffsetDateTime authorizationTimestamp = timestamp(
                record,
                "authorization_timestamp",
                rowNumber
        );
        OffsetDateTime clearingTimestamp = optionalTimestamp(
                record,
                "clearing_timestamp",
                rowNumber
        );

        return new TransactionRow(
                transactionId,
                network,
                amount,
                currency,
                merchantCountry,
                merchantId,
                merchantName,
                mcc,
                cardCountry,
                cardType,
                cardProduct,
                commercialCard,
                channel,
                entryMode,
                authentication,
                authorizationTimestamp,
                clearingTimestamp,
                required(record, "authorization_code", rowNumber),
                required(record, "settlement_status", rowNumber),
                booleanValue(record, "merchant_data_complete", rowNumber),
                booleanValue(record, "recurring", rowNumber),
                booleanValue(record, "refund", rowNumber),
                optionalBoolean(record, "cvv_present", false, rowNumber),
                Collections.unmodifiableMap(new LinkedHashMap<>(record.toMap()))
        );
    }

    public String targetCardType(long rowNumber) {
        if (commercialCard) {
            return "COMMERCIAL";
        }
        String normalized = cardType.toLowerCase(Locale.ROOT);
        if (normalized.contains("debit")) {
            return "DEBIT";
        }
        if (normalized.contains("credit")) {
            return "CREDIT";
        }
        throw invalid(rowNumber, "card_type must describe a debit or credit card");
    }

    public String targetCardCategory() {
        return commercialCard ? "BUSINESS" : "CONSUMER";
    }

    public String targetChannel(long rowNumber) {
        return switch (channel.toLowerCase(Locale.ROOT)) {
            case "pos" -> "POS";
            case "ecommerce", "e-commerce", "online" -> "ECOMMERCE";
            case "moto" -> "MOTO";
            default -> throw invalid(
                    rowNumber,
                    "channel must be pos, ecommerce, or moto"
            );
        };
    }

    public boolean threeDsUsed() {
        String normalized = authentication.toLowerCase(Locale.ROOT);
        return normalized.contains("3ds_authenticated");
    }

    public String rawDataJson() {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : rawData.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"')
                    .append(jsonEscape(entry.getKey()))
                    .append("\":\"")
                    .append(jsonEscape(entry.getValue()))
                    .append('"');
        }
        return json.append('}').toString();
    }

    private static String required(CSVRecord record, String header, long rowNumber) {
        String value = record.get(header);
        if (value == null || value.isBlank()) {
            throw invalid(rowNumber, header + " is required");
        }
        return value.trim();
    }

    private static String countryOrCurrency(
            CSVRecord record,
            String header,
            int expectedLength,
            long rowNumber
    ) {
        String value = required(record, header, rowNumber)
                .toUpperCase(Locale.ROOT);
        if (value.length() != expectedLength || !value.matches("[A-Z]+")) {
            throw invalid(
                    rowNumber,
                    header + " must contain " + expectedLength + " letters"
            );
        }
        return value;
    }

    private static BigDecimal decimal(
            CSVRecord record,
            String header,
            long rowNumber
    ) {
        try {
            return new BigDecimal(required(record, header, rowNumber));
        } catch (NumberFormatException exception) {
            throw invalid(rowNumber, header + " must be a decimal number");
        }
    }

    private static OffsetDateTime timestamp(
            CSVRecord record,
            String header,
            long rowNumber
    ) {
        try {
            return OffsetDateTime.parse(required(record, header, rowNumber));
        } catch (RuntimeException exception) {
            throw invalid(rowNumber, header + " must be an ISO-8601 timestamp");
        }
    }

    private static OffsetDateTime optionalTimestamp(
            CSVRecord record,
            String header,
            long rowNumber
    ) {
        if (!record.isMapped(header)) {
            return null;
        }
        String value = record.get(header);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim());
        } catch (RuntimeException exception) {
            throw invalid(rowNumber, header + " must be an ISO-8601 timestamp");
        }
    }

    private static boolean booleanValue(
            CSVRecord record,
            String header,
            long rowNumber
    ) {
        String value = required(record, header, rowNumber);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw invalid(rowNumber, header + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    private static boolean optionalBoolean(
            CSVRecord record,
            String header,
            boolean defaultValue,
            long rowNumber
    ) {
        if (!record.isMapped(header)) {
            return defaultValue;
        }
        String value = record.get(header);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw invalid(rowNumber, header + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    private static IllegalArgumentException invalid(long rowNumber, String message) {
        return new IllegalArgumentException("CSV row " + rowNumber + ": " + message);
    }

    private static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 8);
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format(
                                Locale.ROOT,
                                "\\u%04x",
                                (int) character
                        ));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
