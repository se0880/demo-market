package ru.sberbank.demo.stocks.inMemory;

import java.util.*;

public class StockOrderTransaction implements AutoCloseable {

    private static final int DEFAULT_COUNT_OF_ACTIVE_ORDERS_PER_STOCK_TYPE = 100000;

    private static final class StockMarketKey {
        private final int cost;
        private final int count;

        public StockMarketKey(int cost, int count) {
            this.cost = cost;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StockMarketKey key = (StockMarketKey) o;
            return cost == key.cost &&
                    count == key.count;
        }

        @Override
        public int hashCode() {
            int result = count;
            result = 31 * result + cost;
            return result;
        }
    }

    private volatile boolean isEnd = false;

    private final Map<String, Customer> customers;
    private final Map<StockType, Map<StockMarketKey, List<StockOrder>>> selling = new EnumMap<>(StockType.class);
    private final Map<StockType, Map<StockMarketKey, List<StockOrder>>> buying = new EnumMap<>(StockType.class);

    public StockOrderTransaction(Map<String, Customer> customers) {
        if (customers == null) {
            throw new IllegalArgumentException("The customers value cannot be null");
        }

        for (StockType stockType : StockType.values()) {
            selling.put(stockType, new HashMap<>(DEFAULT_COUNT_OF_ACTIVE_ORDERS_PER_STOCK_TYPE));
            buying.put(stockType, new HashMap<>(DEFAULT_COUNT_OF_ACTIVE_ORDERS_PER_STOCK_TYPE));
        }

        this.customers = customers;
    }

    public synchronized void addOrder(String customerName, boolean isSelling, StockType stockType, int cost, int count) {
        if (isEnd) throw new IllegalArgumentException("Cannot add order, this transaction has been finished");

        customerName = customerName == null ? "" : customerName.trim();
        final Customer customer = customers.get(customerName);
        if (customer == null) {
            throw new IllegalArgumentException(String.format("Cannot find customer \'%s\'", customerName));
        }

        if (cost <= 0) throw new IllegalArgumentException("The cost must be grater than zero");
        if (count <= 0) throw new IllegalArgumentException("The count must be grater than zero");

        final StockMarketKey key = new StockMarketKey(cost, count);
        if (isSelling) {
            List<StockOrder> stockOrders = buying.get(stockType).get(key);

            StockOrder sold = sell(stockOrders, customer);
            if (sold == null) {
                Map<StockMarketKey, List<StockOrder>> sellingForType = selling.get(stockType);
                List<StockOrder> stockOrderForSelling = sellingForType.computeIfAbsent(key, k -> new LinkedList<>());
                stockOrderForSelling.add(new StockOrder(customer, true, stockType, cost, count));
            } else {
                stockOrders.remove(sold);
            }
        } else {
            Map<StockMarketKey, List<StockOrder>> ordersByCostAndCount = selling.get(stockType);
            List<StockOrder> stockOrders = ordersByCostAndCount.get(key);
            StockOrder bought = buy(stockOrders, customer);
            if (bought == null) {
                Map<StockMarketKey, List<StockOrder>> buyingForType = buying.get(stockType);
                List<StockOrder> stockOrderForBuying = buyingForType.computeIfAbsent(key, k -> new LinkedList<>());
                stockOrderForBuying.add(new StockOrder(customer, false, stockType, cost, count));
            } else {
                stockOrders.remove(bought);
            }
        }
    }

    private StockOrder sell(List<StockOrder> stockOrders, Customer seller) {
        if (stockOrders == null) return null;
        for (StockOrder stockOrder : stockOrders) {
            if (!seller.equals(stockOrder.getCustomer())) {
                stockOrder.getCustomer().writeBuy(stockOrder);
                seller.writeSell(stockOrder);
                stockOrders.remove(stockOrder);
                return stockOrder;
            }
        }
        return null;
    }

    private StockOrder buy(List<StockOrder> stockOrders, Customer buyer) {
        if (stockOrders == null) return null;
        for (StockOrder stockOrder : stockOrders) {
            if (!buyer.equals(stockOrder.getCustomer())) {
                stockOrder.getCustomer().writeSell(stockOrder);
                buyer.writeBuy(stockOrder);
                stockOrders.remove(stockOrder);
                return stockOrder;
            }
        }
        return null;
    }


    public synchronized void end() {
        isEnd = true;
    }

    @Override
    public void close() {
        end();
    }
}
