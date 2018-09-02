package ru.sberbank.demo.stocks.inMemory;

import java.util.Objects;

class StockOrder {
    private final Customer customer;
    private final boolean isSelling;
    private final StockType stockType;
    private final int cost;
    private final int count;

    public StockOrder(Customer customer, boolean isSelling, StockType stockType, int cost, int count) {
        this.customer = customer;
        this.isSelling = isSelling;
        this.stockType = stockType;
        this.cost = cost;
        this.count = count;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isSelling() {
        return isSelling;
    }

    public StockType getStockType() {
        return stockType;
    }

    public int getCost() {
        return cost;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockOrder that = (StockOrder) o;
        return isSelling == that.isSelling &&
                cost == that.cost &&
                count == that.count &&
                Objects.equals(customer, that.customer) &&
                stockType == that.stockType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, isSelling, stockType, cost, count);
    }
}
