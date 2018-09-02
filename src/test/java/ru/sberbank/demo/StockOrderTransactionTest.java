package ru.sberbank.demo;

import org.junit.Assert;
import org.junit.Test;
import ru.sberbank.demo.stocks.inMemory.Customer;
import ru.sberbank.demo.stocks.inMemory.Stock;
import ru.sberbank.demo.stocks.inMemory.StockOrderTransaction;
import ru.sberbank.demo.stocks.inMemory.StockType;

import java.util.HashMap;
import java.util.Map;

public class StockOrderTransactionTest {
    @Test
    public void testAddOrder() {

        final Map<String, Customer> customers = new HashMap<>();

        final String name1 = "C1";
        final String name2 = "C2";

        int customer1Balance = 1222;
        int customer2Balance = 2321;
        int customer1A = 123;
        int customer2A = 233;
        int customer1B = 231;
        int customer2B = 135;
        int customer1C = 123;
        int customer2C = 345;
        int customer1D = 654;
        int customer2D = 123;

        final Customer customer1 = new Customer(name1, customer1Balance, new Stock[]{
                new Stock(StockType.A, customer1A),
                new Stock(StockType.B, customer1B),
                new Stock(StockType.C, customer1C),
                new Stock(StockType.D, customer1D)
        });
        final Customer customer2 = new Customer(name2, customer2Balance, new Stock[]{
                new Stock(StockType.A, customer2A),
                new Stock(StockType.B, customer2B),
                new Stock(StockType.C, customer2C),
                new Stock(StockType.D, customer2D)
        });

        customers.put(customer1.getName(), customer1);
        customers.put(customer2.getName(), customer2);

        final StockOrderTransaction stockOrderTransaction = new StockOrderTransaction(customers);

        stockOrderTransaction.addOrder(name1, true, StockType.A, 2, 7);
        stockOrderTransaction.addOrder(name2, false, StockType.B, 3, 8); //Self selling need to ignore
        stockOrderTransaction.addOrder(name2, false, StockType.A, 2, 7);
        stockOrderTransaction.addOrder(name2, true, StockType.C, 2, 2);
        stockOrderTransaction.addOrder(name2, true, StockType.B, 3, 8);  //Self selling need to ignore
        stockOrderTransaction.addOrder(name1, false, StockType.C, 2, 2);
        stockOrderTransaction.addOrder(name1, true, StockType.D, 5, 8);
        stockOrderTransaction.addOrder(name2, false, StockType.D, 5, 8);

        Assert.assertEquals("Balance of " + name1, customer1Balance + 2 * 7 - 2 * 2 + 5 * 8, customer1.getBalance(), 0);
        Assert.assertEquals("Balance of " + name2, customer2Balance - 2 * 7 + 2 * 2 - 5 * 8, customer2.getBalance(), 0);

        Assert.assertEquals("Count for A of " + name1, customer1A - 7, customer1.getStockValue(StockType.A));
        Assert.assertEquals("Count for A of " + name2, customer2A + 7, customer2.getStockValue(StockType.A));

        Assert.assertEquals("Count for B of " + name1, customer1B, customer1.getStockValue(StockType.B));
        Assert.assertEquals("Count for B of " + name2, customer2B, customer2.getStockValue(StockType.B));

        Assert.assertEquals("Count for C of " + name1, customer1C + 2, customer1.getStockValue(StockType.C));
        Assert.assertEquals("Count for C of " + name2, customer2C - 2, customer2.getStockValue(StockType.C));

        Assert.assertEquals("Count for D of " + name1, customer1D - 8, customer1.getStockValue(StockType.D));
        Assert.assertEquals("Count for D of " + name2, customer2D + 8, customer2.getStockValue(StockType.D));

    }
}
