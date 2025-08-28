package com.example.inshorts.enums;

public enum IntentType {
    CATEGORY("category"),
    SOURCE("source"),
    NEARBY("nearby"),
    SCORE("score"),
    SEARCH("search");

    private final String value;

    IntentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IntentType fromString(String text) {
        for (IntentType intent : IntentType.values()) {
            if (intent.value.equalsIgnoreCase(text)) {
                return intent;
            }
        }
        return SEARCH; // Default to search
    }
}

