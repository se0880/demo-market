package ru.sberbank.demo;

import ru.sberbank.demo.stocks.IStockOrderProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    private static final IStockOrderProcessor stockOrderProcessor = IStockOrderProcessor.INSTANCE;

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.out.println("To using, you must set three parameters: {clients file path} {orders file path} {reult file path}");
            return;
        }

        final Path clients = Paths.get(args[0]);
        final Path orders = Paths.get(args[1]);
        final Path stocks = Paths.get(args[2]);

        stockOrderProcessor.processOrders(clients, orders, stocks);

    }
}
