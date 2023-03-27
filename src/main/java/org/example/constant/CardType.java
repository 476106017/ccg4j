package org.example.constant;

public enum CardType {
    AMULET("护符"),
    FOLLOW("随从"),
    SPELL("法术"),
    EQUIP("装备"),
    ;

    private final String name;

    CardType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
