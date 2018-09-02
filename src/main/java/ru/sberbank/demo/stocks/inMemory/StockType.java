package ru.sberbank.demo.stocks.inMemory;

public enum StockType {
    A, B, C, D;

    public static StockType valueIgnoreCaseOf(String name) {
        if (name == null) return null;
        name = name.trim();
        for (StockType s : values()) {
            if (s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}