package ru.sberbank.demo.stocks;

import ru.sberbank.demo.stocks.inMemory.StockOrderProcessor;

import java.io.IOException;
import java.nio.file.Path;

public interface IStockOrderProcessor {
    void processOrders(Path customerPath, Path orderPath, Path resultPath) throws IOException;

    IStockOrderProcessor INSTANCE = new StockOrderProcessor();
}
