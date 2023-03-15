package org.hk.services;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hk.models.HkRecord;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hk.util.Helper.DELIMITER;
import static org.hk.util.Helper.deleteFile;
import static org.hk.util.Helper.dir;
import static org.hk.util.Helper.rah25;
import static org.hk.util.Helper.rah26;
import static org.hk.util.Helper.rah36;
import static org.hk.util.Helper.rah704;
import static org.hk.util.Helper.rah901;
import static org.hk.util.Helper.round;
import static org.hk.util.Helper.warehouse;


public class ReadFromExcel {
    private static final List<HkRecord> records = new ArrayList<>();
    private static final Map<String, Double> startCount = new HashMap<>();
    private static final Map<String, String> docRecordMap = new HashMap<>();
    private static final Set<String> products = new HashSet<>();
    private static final File[] files = new File(".").listFiles();

    public static List<HkRecord> read() {
        assert files != null;
        for (File f : files) {
            processFile(f);
        }

        return records;
    }

    public static Set<String> getProductValues() {
        return products;
    }

    public static Map<String, Double> getStartCount() {
        return startCount;
    }

    public static Map<String, String> getDocRecordMap() {
        return docRecordMap;
    }

    private static void processFile(File file) {
        String fileName = file.getName();
        if (file.isDirectory() && fileName.equals(dir)) {
            deleteFile(file);
            File saveDir = new File("./" + dir);
            saveDir.mkdir();
        }
        if (fileName.contains("start_count")) {
            initialize(file, true);
            return;
        }
        if (file.isFile() && fileName.contains("xls")) {
            initialize(file, false);
        }
    }

    private static void initialize(File f, boolean isStartCount) {
        try {
            Workbook wb = WorkbookFactory.create(f);
            if (isStartCount) {
                initStartCount(wb);
            } else {
                readAndCreateRecords(wb);
            }
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initStartCount(Workbook wb) {
        for (Row r : wb.getSheetAt(0)) {
            startCount.put(r.getCell(0).getStringCellValue(),
                    round(Double.parseDouble(r.getCell(1).toString()
                            .replace(',', '.')), 2));
        }
    }

    private static void readAndCreateRecords(Workbook wb) {
        for (Row r : wb.getSheetAt(0)) {
            createRecord(r);
        }
    }

    private static void createRecord(Row r) {
        HkRecord record = getHkRecord(r);

        String dt = record.getDt();
        String kt = record.getKt();
        if ((rah26.equals(dt) || rah26.equals(kt)) && record.getCount() != 0) {
            if (rah26.equals(kt)) {
                record.setDateTime(record.getDateTime().plusSeconds(10));
            }
            records.add(record);
            if (record.getProduct() != null) {
                products.add(record.getProduct());
            }
        }
        createDocRecordMap(record, dt, kt);
    }

    private static void createDocRecordMap(HkRecord record, String dt, String kt) {
        String doc = record.getDoc() + DELIMITER + record.getDate();
        if (dt.contains(rah36)) {
            docRecordMap.put(doc, record.getContent1());
        }
        if (kt.contains(rah36) && dt.contains(rah704)) {
            docRecordMap.put(doc, record.getContent4());
        }
    }

    private static HkRecord getHkRecord(Row r) {
        LocalDate date = LocalDate.parse(r.getCell(0).getStringCellValue(),
                DateTimeFormatter.ofPattern("dd.MM.yy"));
        String dt = r.getCell(3).getStringCellValue();
        String kt = r.getCell(4).getStringCellValue();
        double count = r.getCell(6).toString().trim().length() > 0 ?
                r.getCell(6).getNumericCellValue() : 0;

        double sum = r.getCell(5).toString().trim().length() > 0 ?
                r.getCell(5).getNumericCellValue() : 0;
        String[] arr = r.getCell(2).getStringCellValue().split("\n");

        String warehouseFrom = rah26.equals(dt) && rah26.equals(kt) && warehouse.equals(arr[1]) ?
                warehouse : null;
        String warehouseTo = rah26.equals(dt) && rah26.equals(kt) && warehouse.equals(arr[4]) ?
                warehouse : null;
        boolean isBladder = checkIsBladder(arr);

        return HkRecord.builder().doc(r.getCell(1).getStringCellValue())
                .date(date).dateTime(date.atTime(0, 0))
                .warehouseFrom(warehouseFrom).warehouseTo(warehouseTo)
                .product(getProductFromRow(arr, isBladder, dt, kt))
                .content1(arr.length > 1 ? arr[1] : null)
                .content4(arr.length > 5 ? arr[4] : null)
                .dt(dt).kt(kt)
                .count(count).sum(sum)
                .isBladder(isBladder)
                .build();
    }

    private static boolean checkIsBladder(String[] arr) {
        return Arrays.stream(arr).anyMatch(s -> s.contains("міхур"));
    }

    private static String getProductFromRow(String[] arr, boolean isBladder, String dt, String kt) {
        String product = null;
        if (rah26.equals(dt) && !rah26.equals(kt) && warehouse.equals(arr[1])) {
            product = arr[2];
        }
        if (rah26.equals(kt)) {
            if ((rah901.equals(dt) || (rah25.equals(dt) && isBladder)) && warehouse.equals(arr[4])) {
                product = arr[5];
            } else if (rah26.equals(dt) && (warehouse.equals(arr[4]) || warehouse.equals(arr[1]))) {
                if (!arr[2].equals(arr[5])) {
                    product = arr[2];
                } else {
                    product = arr[5];
                }
            }
        }
        return product;
    }
}
