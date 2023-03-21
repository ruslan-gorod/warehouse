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
import static org.hk.util.Helper.RAH_25;
import static org.hk.util.Helper.RAH_26;
import static org.hk.util.Helper.RAH_281;
import static org.hk.util.Helper.RAH_36;
import static org.hk.util.Helper.RAH_63;
import static org.hk.util.Helper.RAH_702;
import static org.hk.util.Helper.RAH_704;
import static org.hk.util.Helper.RAH_901;
import static org.hk.util.Helper.RAH_902;
import static org.hk.util.Helper.WAREHOUSE;
import static org.hk.util.Helper.round;


public class ReadFromExcel {
    private static final List<HkRecord> records = new ArrayList<>();
    private static final Map<String, Double> startCount = new HashMap<>();
    private static final Map<String, String> docRecordMap = new HashMap<>();
    private static final Set<String> products = new HashSet<>();
    private static final File[] files = new File(".").listFiles();

    public static List<HkRecord> read() {
        assert files != null;
        Arrays.stream(files).forEach(ReadFromExcel::processFile);
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
        HkRecord record = createHkRecord(r);

        String dt = record.getDt();
        String kt = record.getKt();
        if ((RAH_26.equals(dt) || RAH_26.equals(kt) || RAH_281.equals(dt) || RAH_281.equals(kt))
                && record.getCount() != 0) {
            if (RAH_26.equals(kt) || RAH_281.equals(kt)) {
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
        if (dt.contains(RAH_36)) {
            docRecordMap.put(doc, record.getContent1());
        }
        if ((kt.contains(RAH_36) && dt.contains(RAH_704))
                || (kt.contains(RAH_36) && dt.contains(RAH_702))
                || (dt.contains(RAH_281) && kt.contains(RAH_63))) {
            docRecordMap.put(doc, record.getContent4());
        }
    }

    private static HkRecord createHkRecord(Row r) {
        LocalDate date = getRecordLocalDate(r);
        String dt = r.getCell(3).getStringCellValue();
        String kt = r.getCell(4).getStringCellValue();
        String[] recordContent = r.getCell(2).getStringCellValue().split("\n");
        boolean isBladder = checkIsBladder(recordContent);

        return HkRecord.builder().doc(r.getCell(1).getStringCellValue())
                .date(date).dateTime(date.atTime(0, 0))
                .warehouseFrom(getWarehouseFrom(dt, kt, recordContent))
                .warehouseTo(getWarehouseTo(dt, kt, recordContent))
                .product(getProductFromRow(recordContent, isBladder, dt, kt))
                .content1(getContentByLength(recordContent, 1, 1))
                .content4(getContentByLength(recordContent, 5, 4))
                .dt(dt).kt(kt)
                .count(getCount(r)).sum(getRecordSum(r))
                .isBladder(isBladder)
                .build();
    }

    private static LocalDate getRecordLocalDate(Row r) {
        return LocalDate.parse(r.getCell(0).getStringCellValue(),
                DateTimeFormatter.ofPattern("dd.MM.yy"));
    }

    private static double getRecordSum(Row r) {
        return r.getCell(5).toString().trim().length() > 0 ?
                r.getCell(5).getNumericCellValue() : 0;
    }

    private static double getCount(Row r) {
        return r.getCell(6).toString().trim().length() > 0 ?
                r.getCell(6).getNumericCellValue() : 0;
    }

    private static String getContentByLength(String[] recordContent, int length, int position) {
        return recordContent.length > length ? recordContent[position] : null;
    }

    private static String getWarehouseTo(String dt, String kt, String[] recordContent) {
        return RAH_26.equals(dt) && RAH_26.equals(kt) && WAREHOUSE.equals(recordContent[4]) ||
                (RAH_281.equals(kt) && WAREHOUSE.equals(recordContent[4])) ? WAREHOUSE : null;
    }

    private static String getWarehouseFrom(String dt, String kt, String[] recordContent) {
        return (RAH_26.equals(dt) && RAH_26.equals(kt) && WAREHOUSE.equals(recordContent[1])) ||
                (RAH_281.equals(dt) && WAREHOUSE.equals(recordContent[1])) ? WAREHOUSE : null;
    }

    private static boolean checkIsBladder(String[] recordContent) {
        return Arrays.stream(recordContent).anyMatch(s -> s.contains("міхур"));
    }

    private static String getProductFromRow(String[] recordContent, boolean isBladder, String dt, String kt) {
        String product = null;
        if (RAH_26.equals(dt) && !RAH_26.equals(kt) && WAREHOUSE.equals(recordContent[1])) {
            product = recordContent[2];
        }
        if ((RAH_281.equals(dt) || RAH_281.equals(kt)) && WAREHOUSE.equals(recordContent[1])) {
            product = recordContent[2];
        }
        if (RAH_26.equals(kt)) {
            if ((RAH_901.equals(dt) || (RAH_25.equals(dt) && isBladder)) && WAREHOUSE.equals(recordContent[4])) {
                product = recordContent[5];
            } else if (RAH_26.equals(dt) && (WAREHOUSE.equals(recordContent[4]) || WAREHOUSE.equals(recordContent[1]))) {
                if (!recordContent[2].equals(recordContent[5])) {
                    product = recordContent[2];
                } else {
                    product = recordContent[5];
                }
            }
        }
        if (RAH_281.equals(kt) && WAREHOUSE.equals(recordContent[4])) {
            if (RAH_902.equals(dt)) {
                product = recordContent[3];
            } else if (RAH_25.equals(dt)) {
                product = recordContent[5];
            } else {
                product = recordContent[2];
            }
        }
        return product;
    }
}
