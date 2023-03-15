package org.hk.util;

import org.hk.models.HkRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Helper {
    private static final Map<Integer, String> monthsNames = new HashMap<>();
    public static final String DELIMITER = "|";
    public static final String DIR = "reports";
    public static final String DIR_YEARS = "reports_years";
    public static final String RAH_25 = "25";
    public static final String RAH_26 = "26";
    public static final String RAH_36 = "36";
    public static final String RAH_704 = "704";
    public static final String RAH_901 = "901";
    public static final String WAREHOUSE = "Склад готової продукції Х";
    private static final List<HkRecord> listRecordsMinusZal = new ArrayList<>();
    public static final boolean isReportByYears = false;

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
            Arrays.stream(Objects.requireNonNull(element.listFiles())).forEach(Helper::deleteFile);
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
