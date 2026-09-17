package org.schemeguard.backend.service.ruleIntepreter;

import org.schemeguard.backend.entity.Transaction;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Component
public class ConditionEvaluator {
    public record Evaluation(List<Map<String, Object>> passed, List<Map<String, Object>> failed) {
        public boolean matches() { return failed.isEmpty(); }
    }

    public Evaluation evaluate(Transaction transaction, Map<String, Object> conditions) {
        List<Map<String, Object>> passed = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();
        if (conditions == null
                || conditions.size() != 1
                || !(conditions.get("all") instanceof List<?> all)
                || all.isEmpty()) {
            throw new IllegalArgumentException("Conditions must contain a nonempty 'all' array only");
        }
        for (Object item : all) {
            if (!(item instanceof Map<?, ?> condition)
                    || !(condition.get("field") instanceof String field)
                    || !(condition.get("operator") instanceof String operator)
                    || !condition.containsKey("value") || condition.get("value") == null) {
                throw new IllegalArgumentException("Each condition requires field, operator and a non-null value");
            }
            Object actual = resolve(transaction, field);
            Object expected = condition.get("value");
            String op = normalize(operator);
            if (!Set.of("equals", "notequals", "greaterthan", "lessthan",
                    "greaterorequalthan", "lessorequalthan", "greaterthanorequal", "lessthanorequal").contains(op)) {
                throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
            boolean matches = actual != null && compare(actual, expected, op);
            Map<String, Object> trace = new LinkedHashMap<>();
            trace.put("field", field);
            trace.put("operator", operator);
            trace.put("expected", expected);
            trace.put("actual", actual);
            if (actual == null) trace.put("reason", "Transaction value is missing");
            (matches ? passed : failed).add(trace);
        }
        return new Evaluation(passed, failed);
    }

    private boolean compare(Object actual, Object expected, String operator) {
        int comparison;
        if (actual instanceof Number) {
            try {
                comparison = new BigDecimal(actual.toString()).compareTo(new BigDecimal(expected.toString()));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Numeric condition requires a numeric value");
            }
        } else {
            if (!operator.equals("equals") && !operator.equals("notequals")) {
                throw new IllegalArgumentException("Ordering operators require numeric fields");
            }
            if (actual instanceof Boolean && !Set.of("true", "false").contains(expected.toString().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Boolean condition requires true or false");
            }
            comparison = actual.toString().compareToIgnoreCase(expected.toString());
        }
        return switch (operator) {
            case "equals" -> comparison == 0;
            case "notequals" -> comparison != 0;
            case "greaterthan" -> comparison > 0;
            case "lessthan" -> comparison < 0;
            case "greaterorequalthan", "greaterthanorequal" -> comparison >= 0;
            case "lessorequalthan", "lessthanorequal" -> comparison <= 0;
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    private Object resolve(Transaction t, String field) {
        return switch (normalize(field)) {
            case "cardtype" -> t.getCardType();
            case "cardcategory" -> t.getCardCategory();
            case "channel" -> t.getChannel();
            case "threedsused" -> t.getThreeDsUsed();
            case "cvvpresent" -> t.getCvvPresent();
            case "amount" -> t.getAmount();
            case "currency", "currencycode" -> t.getCurrencyCode();
            case "merchantcountry" -> t.getMerchantCountry();
            case "issuercountry", "cardcountry" -> t.getIssuerCountry();
            case "mcc" -> t.getMerchant().getMcc();
            case "merchantdatacomplete" -> rawBoolean(t, "merchant_data_complete", "merchantDataComplete");
            case "clearingdelaydays" -> {
                if (t.getClearedAt() == null
                        || t.getAuthorizedAt() == null) yield null;

                Duration delay = Duration.between(t.getAuthorizedAt(), t.getClearedAt());

                if (delay.isNegative()) throw new IllegalArgumentException("Clearing precedes authorization");

                yield BigDecimal.valueOf(delay.getSeconds()).add(BigDecimal.valueOf(delay.getNano(), 9))
                        .divide(BigDecimal.valueOf(86400 /* 24h * 60m * 60s */), java.math.MathContext.DECIMAL128);
            }
            default -> throw new IllegalArgumentException("Unsupported condition field: " + field);
        };
    }

    private Boolean rawBoolean(Transaction t, String snake, String camel) {
        JsonNode raw = t.getRawData();
        if (raw == null) return null;
        JsonNode value = raw.get(snake);
        if (value == null) value = raw.get(camel);
        if (value == null || value.isNull()) return null;
        String text = value.asText().trim();
        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;
        return null;
    }

    private String normalize(String value) {
        return value.replace("_", "").toLowerCase(Locale.ROOT);
    }
}

