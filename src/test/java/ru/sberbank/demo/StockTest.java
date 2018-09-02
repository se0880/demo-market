package ru.sberbank.demo;

import org.junit.Assert;
import org.junit.Test;
import ru.sberbank.demo.stocks.inMemory.Stock;
import ru.sberbank.demo.stocks.inMemory.StockType;

public class StockTest {
    @Test
    public void testIncrease() {
        Stock stock = new Stock(StockType.A, 10);
        stock.increase(111111);
        Assert.assertEquals("Increase test", stock.getValue(), 10 + 111111);
    }

    @Test
    public void testDecrease() {
        Stock stock = new Stock(StockType.B, 2333);
        stock.decrease(1332);
        Assert.assertEquals("Decrease test", stock.getValue(), 2333 - 1332);
    }
}
