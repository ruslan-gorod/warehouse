package org.hk;

import org.hk.dao.WorkWithDB;
import org.hk.models.HkRecord;
import org.hk.services.ReadFromExcel;
import org.hk.services.WriteToExcel;
import org.hk.util.Helper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        Helper.initMonthNames();

        List<HkRecord> records = ReadFromExcel.read();
        System.out.println("records.size = " + records.size());
        Set<String> products = ReadFromExcel.getProductValues();
        System.out.println("products.size = " + products.size());
        WorkWithDB.writeRecords(records);

        WriteToExcel.write(products);

        System.out.println("Completed");

        printTime(startLocalDateTime);
        System.exit(0);
    }

    private static void printTime(LocalDateTime startLocalDateTime) {
        long millis = Duration.between(startLocalDateTime, LocalDateTime.now()).toMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        String time = String.format("%d minutes %d seconds", minutes,
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes));
        System.out.printf("Time taken: %s%n", time);
    }
}
