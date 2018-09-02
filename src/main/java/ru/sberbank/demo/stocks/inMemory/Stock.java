package ru.sberbank.demo.stocks.inMemory;

import java.util.concurrent.atomic.AtomicInteger;

public class Stock {
    private final StockType stockType;
    private final AtomicInteger value = new AtomicInteger();

    public Stock(StockType stockType, int value) {
        this.stockType = stockType;
        this.value.set(value);
    }

    public StockType getStockType() {
        return stockType;
    }

    public int getValue() {
        return value.get();
    }

    public void setValue(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Balance cannot be <0");
        }
        this.value.set(value);
    }

    public void increase(int count) {
        this.value.getAndAdd(count);
    }

    public void decrease(int count) {
        if (this.value.addAndGet(-count) < 0) {
     //       throw new IllegalArgumentException("Balance cannot be <0");
        }
    }
}