package ru.sberbank.demo.stocks.inMemory;

import ru.sberbank.demo.stocks.IStockOrderProcessor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class StockOrderProcessor implements IStockOrderProcessor {

    private static final int AVERAGE_FILE_ROW_SIZE = 12;

    private static final Charset DEFAULT_FILE_CHARSET = StandardCharsets.UTF_8;

    private static final NumberFormat DEFAULT_NUMBER_FORMAT;

    private static final int MAX_LENGTH_ROW = 1000;

    private static final long MAX_COUNT_OF_ROWS_TO_PROCESS = (int) ((0.3 * Runtime.getRuntime().maxMemory()) / AVERAGE_FILE_ROW_SIZE);

    private static final String SELLING_CODE = "s".trim().toLowerCase();
    private static final String BUYING_CODE = "b".trim().toLowerCase();

    private static final String FIELDS_DELIMITER = "\t";
    private static final String LINE_DELIMITER = "\n";

    static {
        DEFAULT_NUMBER_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);
        if (DEFAULT_NUMBER_FORMAT instanceof DecimalFormat) {
            DecimalFormat decimalFormat = ((DecimalFormat)DEFAULT_NUMBER_FORMAT);
            decimalFormat.setDecimalSeparatorAlwaysShown(false);
            decimalFormat.setGroupingUsed(false);
        }
    }

    private static Map<String, Customer> loadFileABCD(Path file) throws IOException {

        long size = Files.size(file);
        if (size / AVERAGE_FILE_ROW_SIZE > MAX_COUNT_OF_ROWS_TO_PROCESS) {
            throw new IOException("Too many customers for such Heap and this implementation, please increase Heap\'s size");
        }

        final Map<String, Customer> res = new HashMap<>((int) (size / AVERAGE_FILE_ROW_SIZE));

        try (InputStream clientsStream = Files.newInputStream(file)) {
            try (InputStreamReader clientsReader = new InputStreamReader(clientsStream, DEFAULT_FILE_CHARSET)) {
                try (BufferedReader reader = new BufferedReader(clientsReader)) {
                    String s;
                    while ((s = reader.readLine()) != null) {
                        if (s.isEmpty()) continue;

                        if (s.length() > MAX_LENGTH_ROW) {
                            throw new IOException("Illegal row size");
                        }

                        String[] parts = s.split("\t");
                        if (parts.length != 6) {
                            throw new IOException(String.format("Illegal row format \'%s\', incorrect tabs count", s));
                        }

                        String name = parts[0].trim().toUpperCase();
                        if (name.isEmpty()) {
                            throw new IOException(String.format("The name of customer cannot be empty with row \'%s\'", s));
                        }


                        Stock[] stocks = new Stock[4];
                        double balance;
                        try {
                            balance = DEFAULT_NUMBER_FORMAT.parse(parts[1]).doubleValue();
                            stocks[0] = new Stock(StockType.A, DEFAULT_NUMBER_FORMAT.parse(parts[2]).intValue());
                            stocks[1] = new Stock(StockType.B, DEFAULT_NUMBER_FORMAT.parse(parts[3]).intValue());
                            stocks[2] = new Stock(StockType.C, DEFAULT_NUMBER_FORMAT.parse(parts[4]).intValue());
                            stocks[3] = new Stock(StockType.D, DEFAULT_NUMBER_FORMAT.parse(parts[5]).intValue());
                        } catch (ParseException ex) {
                            throw new IOException(String.format("Cannot parse numbers for line \'%s\'", s), ex);
                        }

                        final Customer c = new Customer(name, balance, stocks);

                        if (res.put(c.getName(), c) != null) {
                            throw new IOException(String.format("Customer name duplication found with \'%s\'", c.getName()));
                        }

                    }
                }
            }
        }

        return Collections.unmodifiableMap(res);
    }

    private void processOrders(StockOrderTransaction transaction, Path orderPath) throws IOException {

        try (InputStream clientsStream = Files.newInputStream(orderPath)) {
            try (InputStreamReader clientsReader = new InputStreamReader(clientsStream, DEFAULT_FILE_CHARSET)) {
                try (BufferedReader reader = new BufferedReader(clientsReader)) {
                    String s;
                    while ((s = reader.readLine()) != null) {
                        if (s.isEmpty()) continue;

                        if (s.length() > MAX_LENGTH_ROW) {
                            throw new IOException("Illegal row size");
                        }

                        String[] parts = s.split("\t");
                        if (parts.length != 5) {
                            throw new IOException(String.format("Illegal row format \'%s\', incorrect tabs count", s));
                        }

                        parts[1] = parts[1].trim();
                        boolean isSelling = parts[1].equalsIgnoreCase(SELLING_CODE);
                        if (!isSelling && !parts[1].equalsIgnoreCase(BUYING_CODE)) {
                            throw new IOException("Unknown operation code");
                        }
                        final StockType stockType = StockType.valueIgnoreCaseOf(parts[2]);
                        if (stockType == null) {
                            throw new IOException(String.format("Cannot find stock type \'%s\'", parts[2]));
                        }

                        int cost, count;
                        try {
                            cost = DEFAULT_NUMBER_FORMAT.parse(parts[3]).intValue();
                            count = DEFAULT_NUMBER_FORMAT.parse(parts[4]).intValue();
                        } catch (ParseException ex) {
                            throw new IOException(String.format("Cannot parse cost or count value with \'%s\'", s), ex);
                        }

                        transaction.addOrder(parts[0], isSelling, stockType, cost, count);
                    }
                }
            }
        }
    }

    private void writeFileABCD(Path resultPath, Map<String, Customer> customers) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(resultPath)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, DEFAULT_FILE_CHARSET)) {

                for (Map.Entry<String, Customer> customerAndName : customers.entrySet()) {
                    Customer c = customerAndName.getValue();

                    writer.append(c.getName());
                    writer.append(FIELDS_DELIMITER);

                    writer.append(DEFAULT_NUMBER_FORMAT.format(c.getBalance()));
                    writer.append(FIELDS_DELIMITER);

                    writer.append(DEFAULT_NUMBER_FORMAT.format(c.getStockValue(StockType.A)));
                    writer.append(FIELDS_DELIMITER);
                    writer.append(DEFAULT_NUMBER_FORMAT.format(c.getStockValue(StockType.B)));
                    writer.append(FIELDS_DELIMITER);
                    writer.append(DEFAULT_NUMBER_FORMAT.format(c.getStockValue(StockType.C)));
                    writer.append(FIELDS_DELIMITER);
                    writer.append(DEFAULT_NUMBER_FORMAT.format(c.getStockValue(StockType.D)));
                    writer.append(LINE_DELIMITER);
                }

                writer.flush();
            }
        }
    }

    public void processOrders(Path customerPath, Path orderPath, Path resultPath) throws IOException {
        final Map<String, Customer> customers = loadFileABCD(customerPath);
        try (StockOrderTransaction transaction = new StockOrderTransaction(customers)) {
            processOrders(transaction, orderPath);
            transaction.end();
            writeFileABCD(resultPath, customers);
        }
    }
}
