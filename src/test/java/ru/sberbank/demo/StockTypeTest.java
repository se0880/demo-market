package ru.sberbank.demo;

import org.junit.Assert;
import org.junit.Test;
import ru.sberbank.demo.stocks.inMemory.StockType;

public class StockTypeTest {
    @Test
    public void testValueIgnoreCaseOf() {
        Assert.assertEquals("Test parser for A", StockType.A, StockType.valueIgnoreCaseOf("A"));
        Assert.assertEquals("Test parser for B", StockType.B, StockType.valueIgnoreCaseOf("B"));
        Assert.assertEquals("Test parser for C", StockType.C, StockType.valueIgnoreCaseOf("C"));
        Assert.assertEquals("Test parser for D", StockType.D, StockType.valueIgnoreCaseOf("D"));
    }
}
