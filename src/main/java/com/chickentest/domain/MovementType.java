package com.chickentest.domain;

public enum MovementType {
    PURCHASE("purchase"),
    SALE("sale"),
    SYSTEM("system");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static MovementType fromDescription(String description) {
        for (MovementType type : values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown movement type: " + description);
    }
}
