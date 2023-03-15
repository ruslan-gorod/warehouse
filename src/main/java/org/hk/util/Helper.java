package org.hk.util;

import org.hk.models.HkRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Helper {
    private static final Map<Integer, String> monthsNames = new HashMap<>();
    public static final String DELIMITER = "|";
    public static final String dir = "reports";
    public static final String rah25 = "25";
    public static final String rah26 = "26";
    public static final String rah36 = "36";
    public static final String rah704 = "704";
    public static final String rah901 = "901";
    public static final String warehouse = "Склад готової продукції Х";
    private static final List<HkRecord> listRecordsMinusZal = new ArrayList<>();

    public static void initMonthNames() {
        monthsNames.put(1, "січень");
        monthsNames.put(2, "лютий");
        monthsNames.put(3, "березень");
        monthsNames.put(4, "квітень");
        monthsNames.put(5, "травень");
        monthsNames.put(6, "червень");
        monthsNames.put(7, "липень");
        monthsNames.put(8, "серпень");
        monthsNames.put(9, "вересень");
        monthsNames.put(10, "жовтень");
        monthsNames.put(11, "листопад");
        monthsNames.put(12, "грудень");
    }

    public static Map<Integer, String> getMonthsNames() {
        return monthsNames;
    }

    public static void deleteFile(File element) {
        if (element.exists() && element.isDirectory()) {
            for (File sub : Objects.requireNonNull(element.listFiles())) {
                deleteFile(sub);
            }
        }
        element.delete();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static List<HkRecord> getListRecordsMinusZal() {
        return listRecordsMinusZal;
    }
}
