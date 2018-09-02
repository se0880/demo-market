package ru.sberbank.demo.stocks.inMemory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Customer {


    private final String name;
    private volatile double balance;
    private final Map<StockType, Stock> stocks;

    public Customer(String name, double balance, Stock[] stocks) {
        if (stocks == null) {
            throw new IllegalArgumentException("Stocks cannot be null");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        this.name = name;
        this.balance = balance;

        EnumMap<StockType, Stock> stocksMap = new EnumMap<>(StockType.class);
        for (Stock s : stocks) {
            stocksMap.put(s.getStockType(), s);
        }
        this.stocks = Collections.unmodifiableMap(stocksMap);
    }

    public String getName() {
        return name;
    }


    public synchronized void writeBuy(StockOrder stockOrder) {
        balance -= stockOrder.getCost() * stockOrder.getCount();
        if (balance < 0) {
//            throw new IllegalArgumentException("Balance cannot < 0");
        }
        Stock s = stocks.get(stockOrder.getStockType());
        if (s == null) {
            throw new IllegalArgumentException("Cannot find stock");
        }
        s.increase(stockOrder.getCount());
    }

    public synchronized void writeSell(StockOrder stockOrder) {
        balance += stockOrder.getCost() * stockOrder.getCount();
        Stock s = stocks.get(stockOrder.getStockType());
        if (s == null) {
            throw new IllegalArgumentException("Cannot find stock");
        }
        s.decrease(stockOrder.getCount());

    }

    public double getBalance() {
        return this.balance;
    }

    public int getStockValue(StockType stockType) {
        Stock s = stocks.get(stockType);
        if (s == null) {
            throw new IllegalArgumentException("Cannot find stock");
        }
        return s.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(name, customer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
