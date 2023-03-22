package org.hk.services;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hk.models.HkRecord;
import org.hk.models.HkRecordMainValue;

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
        HkRecordMainValue value = createHkRecordMainValue(r);

        return HkRecord.builder()
                .dt(value.getDt())
                .kt(value.getKt())
                .count(getCount(r))
                .sum(getRecordSum(r))
                .date(value.getDate())
                .dateTime(value.getDate().atTime(0, 0))
                .content1(getContentByLength(value, 1, 1))
                .content4(getContentByLength(value, 5, 4))
                .doc(getStringCellValueByPosition(r, 1))
                .warehouseFrom(getWarehouseFrom(value))
                .warehouseTo(getWarehouseTo(value))
                .product(getProductFromRow(value))
                .isBladder(value.isBladder())
                .build();
    }

    private static HkRecordMainValue createHkRecordMainValue(Row r) {
        String[] recordContent = getStringCellValueByPosition(r, 2).split("\n");
        return HkRecordMainValue.builder()
                .date(getRecordLocalDate(r))
                .recordContent(recordContent)
                .isBladder(checkIsBladder(recordContent))
                .dt(getStringCellValueByPosition(r, 3))
                .kt(getStringCellValueByPosition(r, 4))
                .build();
    }

    private static String getStringCellValueByPosition(Row r, int position) {
        return r.getCell(position).getStringCellValue();
    }

    private static LocalDate getRecordLocalDate(Row r) {
        return LocalDate.parse(r.getCell(0).getStringCellValue(),
                DateTimeFormatter.ofPattern("dd.MM.yy"));
    }

    private static double getRecordSum(Row r) {
        return getaDoubleValueByPosition(r, 5);
    }

    private static double getCount(Row r) {
        return getaDoubleValueByPosition(r, 6);
    }

    private static double getaDoubleValueByPosition(Row r, int position) {
        return r.getCell(position).toString().trim().length() > 0 ?
                r.getCell(position).getNumericCellValue() : 0;
    }

    private static String getContentByLength(HkRecordMainValue value, int length, int position) {
        return value.getRecordContent().length > length ? value.getRecordContent()[position] : null;
    }

    private static String getWarehouseFrom(HkRecordMainValue value) {
        return getWarehouseByPosition(value, 1);
    }

    private static String getWarehouseTo(HkRecordMainValue value) {
        return getWarehouseByPosition(value, 4);
    }

    private static String getWarehouseByPosition(HkRecordMainValue value, int position) {
        String dt = value.getDt();
        String kt = value.getKt();
        return (RAH_26.equals(dt) && RAH_26.equals(kt) && isWarehouseCorrect(value, position)) ||
                (RAH_281.equals(dt) && isWarehouseCorrect(value, position)) ? WAREHOUSE : null;
    }

    private static boolean isWarehouseCorrect(HkRecordMainValue value, int position) {
        return WAREHOUSE.equals(value.getRecordContent()[position]);
    }

    private static boolean checkIsBladder(String[] recordContent) {
        return Arrays.stream(recordContent).anyMatch(s -> s.contains("міхур"));
    }

    private static String getProductFromRow(HkRecordMainValue value) {
        String dt = value.getDt();
        String kt = value.getKt();
        String[] recordContent = value.getRecordContent();
        int position = 0;
        if (RAH_26.equals(dt) && !RAH_26.equals(kt) && isWarehouseCorrect(value, 1)) {
            position = 2;
        }
        if ((RAH_281.equals(dt) || RAH_281.equals(kt)) && isWarehouseCorrect(value, 1)) {
            position = 2;
        }
        if (RAH_26.equals(kt)) {
            if ((RAH_901.equals(dt) || (RAH_25.equals(dt) && value.isBladder())) && isWarehouseCorrect(value, 4)) {
                position = 5;
            } else if (RAH_26.equals(dt) && (isWarehouseCorrect(value, 4) || isWarehouseCorrect(value, 1))) {
                position = !recordContent[2].equals(recordContent[5]) ? 2 : 5;
            }
        }

        if (RAH_281.equals(kt) && isWarehouseCorrect(value, 4)) {
            if (RAH_902.equals(dt)) {
                position = 3;
            } else if (RAH_25.equals(dt)) {
                position = 5;
            } else {
                position = 2;
            }
        }
        return position > 0 ? recordContent[position] : null;
    }
}
