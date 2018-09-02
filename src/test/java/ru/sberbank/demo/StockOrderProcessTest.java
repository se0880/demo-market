package ru.sberbank.demo;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ru.sberbank.demo.stocks.IStockOrderProcessor;
import ru.sberbank.demo.stocks.inMemory.StockOrderProcessor;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class StockOrderProcessTest {

    private static class TestRecord {
        double balance = 0;
        int A = 0;
        int B = 0;
        int C = 0;
        int D = 0;
    }

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final NumberFormat DEFAULT_NUMBER_FORMAT;

    private static final String FIELD_DELIMITER = "\t";

    private static final String END_ROW_DELIMITER = "\n";

    static {
        DEFAULT_NUMBER_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);
        if (DEFAULT_NUMBER_FORMAT instanceof DecimalFormat) {
            DecimalFormat decimalFormat = ((DecimalFormat) DEFAULT_NUMBER_FORMAT);
            decimalFormat.setDecimalSeparatorAlwaysShown(false);
            decimalFormat.setGroupingUsed(false);
        }
    }


    @Test
    public void testCheckSum() throws IOException {

        final Path clientsFile = Files.createTempFile("clients", ".txt");
        final Path ordersFile = Files.createTempFile("orders", ".txt");
        final Path resultFile = Files.createTempFile("result", ".txt");
        final int clientsCount = 100;
        final int ordersCount = clientsCount * 1000;
        final char[] codes = new char[]{'A', 'B', 'C', 'D'};

        final TestRecord initRecord = new TestRecord();

        try {
            try (OutputStream stream = Files.newOutputStream(clientsFile)) {
                try (OutputStreamWriter writer = new OutputStreamWriter(stream, DEFAULT_CHARSET)) {
                    for (int i = 0; i < clientsCount; i++) {

                        int balance = Math.abs(((i + 2781) ^ (i << 24) * (i + 83) * 31) % 10000);
                        int a = Math.abs(((i + 61) ^ (i + 1 << 24) * (i + 83) * 31) % 10000);
                        int b = Math.abs(((i + 31) ^ (i + 1 << 24) * (i + 17) * 5) % 10000);
                        int c = Math.abs(((i + 11) ^ (i + 1 << 24) * (i + 31) * 2) % 10000);
                        int d = Math.abs(((i + 5) ^ (i + 1 << 24) * (i + 61) * 11) % 10000);

                        writer.append("C").append(DEFAULT_NUMBER_FORMAT.format(i));
                        writer.append(FIELD_DELIMITER);
                        writer.append(DEFAULT_NUMBER_FORMAT.format(balance));
                        writer.append(FIELD_DELIMITER);
                        writer.append(DEFAULT_NUMBER_FORMAT.format(a));
                        writer.append(FIELD_DELIMITER);
                        writer.append(DEFAULT_NUMBER_FORMAT.format(b));
                        writer.append(FIELD_DELIMITER);
                        writer.append(DEFAULT_NUMBER_FORMAT.format(c));
                        writer.append(FIELD_DELIMITER);
                        writer.append(DEFAULT_NUMBER_FORMAT.format(d));
                        writer.append(END_ROW_DELIMITER);

                        initRecord.balance += balance;
                        initRecord.A += a;
                        initRecord.B += b;
                        initRecord.C += c;
                        initRecord.D += d;
                    }

                    writer.flush();
                }
            }


            try (OutputStream stream = Files.newOutputStream(ordersFile)) {
                try (OutputStreamWriter writer = new OutputStreamWriter(stream, DEFAULT_CHARSET)) {
                    for (int i = 0; i < ordersCount; i++) {
                        writer.append("C" + DEFAULT_NUMBER_FORMAT.format(i & (clientsCount - 1)));
                        writer.append(FIELD_DELIMITER);
                        writer.append(((i * 8 + 1000) << i >>> 2) % 2 > 0 ? "s" : "b");
                        writer.append(FIELD_DELIMITER);

                        writer.append(codes[(i + i + i / 7) & codes.length - 1]);
                        writer.append(FIELD_DELIMITER);

                        writer.append(DEFAULT_NUMBER_FORMAT.format(Math.abs(i & (i - 1) * 3 % 100) + 1));
                        writer.append(FIELD_DELIMITER);
                        writer.append(DEFAULT_NUMBER_FORMAT.format(Math.abs(i & (i - 2) * 5 % 100) + 1));
                        writer.append(END_ROW_DELIMITER);
                    }

                    writer.flush();
                }
            }

            IStockOrderProcessor stockOrderProcessor = new StockOrderProcessor();
            stockOrderProcessor.processOrders(clientsFile, ordersFile, resultFile);

            final TestRecord resultRecord = new TestRecord();

            Files.lines(resultFile, DEFAULT_CHARSET).forEach(x -> {
                String[] s = x.split(FIELD_DELIMITER);

                resultRecord.balance += Integer.parseInt(s[1]);
                resultRecord.A += Integer.parseInt(s[2]);
                resultRecord.B += Integer.parseInt(s[3]);
                resultRecord.C += Integer.parseInt(s[4]);
                resultRecord.D += Integer.parseInt(s[5]);
            });

            Assert.assertEquals("Check sum of balance", initRecord.balance, resultRecord.balance, 0);
            Assert.assertEquals("Check sum of A",initRecord.A, resultRecord.A);
            Assert.assertEquals("Check sum of B",initRecord.B, resultRecord.B);
            Assert.assertEquals("Check sum of C",initRecord.C, resultRecord.C);
            Assert.assertEquals("Check sum of D",initRecord.D, resultRecord.D);

        }  finally {
            Files.deleteIfExists(clientsFile);
            Files.deleteIfExists(ordersFile);
            Files.deleteIfExists(resultFile);
        }
    }

}
