package org.hk.services;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hk.dao.WorkWithDB;
import org.hk.models.HkRecord;
import org.hk.models.Operation;
import org.hk.util.HibernateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hk.util.Helper.DELIMITER;
import static org.hk.util.Helper.DIR;
import static org.hk.util.Helper.DIR_YEARS;
import static org.hk.util.Helper.RAH_25;
import static org.hk.util.Helper.RAH_26;
import static org.hk.util.Helper.RAH_281;
import static org.hk.util.Helper.deleteFile;
import static org.hk.util.Helper.getListRecordsMinusZal;
import static org.hk.util.Helper.getMonthsNames;
import static org.hk.util.Helper.isReportByYears;
import static org.hk.util.Helper.round;

public class WriteToExcel {

    public static void write(Set<String> products) {
        if (isReportByYears) {
            deleteFile(new File(DIR_YEARS));
        } else {
            deleteFile(new File(DIR));
        }

        LocalDate startDate = WorkWithDB.getDateFromDB("MIN");
        LocalDate endDate = WorkWithDB.getDateFromDB("MAX");

        products.parallelStream().forEach(product -> saveProductReport(startDate, endDate, product));
    }

    private static void saveProductReport(LocalDate startDate, LocalDate endDate, String product) {
        Operation operation = createOperation(product);
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            int startMonth = year == startDate.getYear() ? startDate.getMonthValue() : 1;
            int endMonth = year == endDate.getYear() ? endDate.getMonthValue() : 12;

            for (int month = startMonth; month <= endMonth; month++) {
                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                    createAndSaveFile(year, month, product, operation, session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Operation createOperation(String product) {
        Double result = ReadFromExcel.getStartCount().get(product);
        return new Operation(0.0, 0.0, result != null ? result : 0.0);
    }

    private static void createAndSaveFile(int year, int month, String product, Operation operation, Session session) {
        try {
            File report = isReportByYears ?
                    getFileReportToSaveByYears(year, month, product) :
                    getFileReportToSave(year, month, product);
            FileOutputStream fos = new FileOutputStream(report);
            saveReportToExcel(year, month, product, operation, session, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveReportToExcel(int year, int month, String product, Operation operation,
                                          Session session, FileOutputStream fos) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(DIR);
            createReportHeader(sheet, product, month, year, operation);
            int rowNumber = addRowsToReport(sheet, product, month, year, operation, session);
            createReportFooter(rowNumber, sheet, operation);
            workbook.write(fos);
        }
    }

    private static File getFileReportToSave(int year, int month, String product) {
        String folderName = DIR + "/" + product.replace("/", " ")
                .replace(" ", "_") + "/" + year;
        createReportFolder(folderName);
        return new File(folderName + "/" + month + ".xlsx");
    }

    private static File getFileReportToSaveByYears(int year, int month, String product) {
        String folderName = DIR_YEARS + "/" + year + "/" + month;
        String fileName = product.replace("/", " ").replace(" ", "_");
        createReportFolder(folderName);
        return new File(folderName + "/" + fileName + ".xlsx");
    }

    private static void createReportFolder(String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private static void createReportHeader(XSSFSheet sheet, String product, int month, int year, Operation operation) {
        Row row0 = sheet.createRow(0);
        Cell cell00 = row0.createCell(0);
        cell00.setCellValue("Товариство з обмеженою відловідальністю \"Хінкель-Когут\"");

        Row row1 = sheet.createRow(1);
        Cell cell10 = row1.createCell(0);
        cell10.setCellValue("КАРТКА СКЛАДСЬКОГО ОБЛІКУ");
        CellStyle styleCenter10 = cell10.getSheet().getWorkbook().createCellStyle();
        setCenterInStyle(styleCenter10);
        Font font10 = cell10.getSheet().getWorkbook().createFont();
        font10.setFontHeightInPoints((short) 14);
        styleCenter10.setFont(font10);
        cell10.setCellStyle(styleCenter10);

        Row row2 = sheet.createRow(2);
        Cell cell20 = row2.createCell(0);
        cell20.setCellValue("Місце зберігання: Склад готової продукції Х. ТМЦ: " + product);
        CellStyle styleCenter20 = cell20.getSheet().getWorkbook().createCellStyle();
        setCenterInStyle(styleCenter20);
        XSSFFont font20 = (XSSFFont) cell20.getSheet().getWorkbook().createFont();
        font20.setBold(true);
        styleCenter20.setFont(font20);
        cell20.setCellStyle(styleCenter20);

        Row row3 = sheet.createRow(3);
        Cell cell30 = row3.createCell(0);
        cell30.setCellValue("за " + getMonthsNames().get(month) + " " + year + "р.");
        CellStyle styleCenter30 = cell30.getSheet().getWorkbook().createCellStyle();
        setCenterInStyle(styleCenter30);
        Font font30 = cell30.getSheet().getWorkbook().createFont();
        font30.setItalic(true);
        styleCenter30.setFont(font30);
        cell30.setCellStyle(styleCenter30);

        IntStream.range(0, 4)
                .mapToObj(i -> new CellRangeAddress(i, i, 0, 7))
                .forEach(sheet::addMergedRegion);

        Row row5 = sheet.createRow(5);
        Cell cell50 = row5.createCell(0);

        cell50.setCellValue("Сальдо на початок: " + round(operation.getResult(), 2));

        Row row6 = sheet.createRow(6);
        Cell cell60 = row6.createCell(0);
        String odVym = (product.contains("іхур") || product.contains("инюга")) ? "шт." : "метр.";
        cell60.setCellValue("одиниця виміру: " + odVym);

        Row row7 = sheet.createRow(7);
        Cell cell70 = row7.createCell(0);
        Cell cell71 = row7.createCell(1);
        Cell cell72 = row7.createCell(2);
        Cell cell73 = row7.createCell(3);
        Cell cell74 = row7.createCell(4);
        Cell cell75 = row7.createCell(5);
        Cell cell76 = row7.createCell(6);
        Cell cell77 = row7.createCell(7);

        CellStyle styleCenter70 = cell70.getSheet().getWorkbook().createCellStyle();
        setCenterInStyle(styleCenter70);
        XSSFFont font70 = (XSSFFont) cell70.getSheet().getWorkbook().createFont();
        font70.setBold(true);
        styleCenter70.setFont(font70);
        styleCenter70.setBorderBottom(BorderStyle.MEDIUM);
        styleCenter70.setBorderTop(BorderStyle.MEDIUM);
        styleCenter70.setBorderLeft(BorderStyle.MEDIUM);
        styleCenter70.setBorderRight(BorderStyle.MEDIUM);
        cell70.setCellValue("№ з/п");
        cell70.setCellStyle(styleCenter70);
        cell71.setCellValue("Дата");
        cell71.setCellStyle(styleCenter70);
        cell72.setCellValue("Документ, номер");
        cell72.setCellStyle(styleCenter70);
        cell73.setCellValue("Кому відпущено / Від кого отримано");
        cell73.setCellStyle(styleCenter70);
        cell74.setCellValue("Прихід");
        cell74.setCellStyle(styleCenter70);
        cell75.setCellValue("Розхід");
        cell75.setCellStyle(styleCenter70);
        cell76.setCellValue("Залишок");
        cell76.setCellStyle(styleCenter70);
        cell77.setCellValue("Примітка");
        cell77.setCellStyle(styleCenter70);
    }

    private static int addRowsToReport(XSSFSheet sheet, String product, int month, int year, Operation operation, Session session) {
        List<HkRecord> records = WorkWithDB.getReportFromDb(session, month, year, product);
        int num = 8;
        for (HkRecord record : records) {
            String dt = record.getDt();
            String kt = record.getKt();
            double count = record.getCount();

            Row row = sheet.createRow(num);

            Cell cellNumberOfRow = row.createCell(0);
            CellStyle style = getCellStyle(cellNumberOfRow);
            cellNumberOfRow.setCellStyle(style);
            cellNumberOfRow.setCellValue(num - 7);
            num++;

            Cell cellDate = row.createCell(1);
            cellDate.setCellValue(record.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            cellDate.setCellStyle(style);

            Cell cellDocWithNumber = row.createCell(2);
            cellDocWithNumber.setCellValue(getDocName(record));
            cellDocWithNumber.setCellStyle(style);

            Cell cellPartner = row.createCell(3);
            cellPartner.setCellValue(getPartnerForOrder(record));
            cellPartner.setCellStyle(style);

            Cell cellIn = row.createCell(4);
            double zal = operation.getResult();
            if (RAH_26.equals(dt) && !RAH_26.equals(kt)) {
                cellIn.setCellValue(count);
                zal += round(count, 2);
                operation.setIn(operation.getIn() + count);
            }
            if (RAH_281.equals(dt) && !RAH_281.equals(kt)) {
                cellIn.setCellValue(count);
                zal += round(count, 2);
                operation.setIn(operation.getIn() + count);
            }
            cellIn.setCellStyle(style);

            Cell cellOut = row.createCell(5);
            if (RAH_26.equals(kt) && !RAH_26.equals(dt) && !RAH_25.equals(dt)) {
                if (count < 0) {
                    count = count * (-1);
                    cellIn.setCellValue(count);
                    zal += round(count, 2);
                    operation.setIn(operation.getIn() + count);
                } else {
                    cellOut.setCellValue(count);
                    zal -= round(count, 2);
                    operation.setOut(operation.getOut() + count);
                }
            }
            if (RAH_25.equals(dt) && RAH_26.equals(kt) && record.isBladder()) {
                cellOut.setCellValue(count);
                zal -= round(count, 2);
                operation.setOut(operation.getOut() + count);
            }
            if (RAH_26.equals(dt) && RAH_26.equals(kt)) {
                if (record.getWarehouseFrom() != null) {
                    cellIn.setCellValue(count);
                    zal += round(count, 2);
                    operation.setIn(operation.getIn() + count);
                } else if (record.getWarehouseTo() != null) {
                    cellOut.setCellValue(count);
                    zal -= round(count, 2);
                    operation.setOut(operation.getOut() + count);
                }
            }
            if (RAH_281.equals(kt)) {
                cellOut.setCellValue(count);
                zal -= round(count, 2);
                operation.setOut(operation.getOut() + count);
            }
            cellOut.setCellStyle(style);

            if (zal < 0) {
                getListRecordsMinusZal().add(record);
            }

            Cell cellTotal = row.createCell(6);
            cellTotal.setCellValue(zal);
            cellTotal.setCellStyle(style);

            Cell cellNote = row.createCell(7);
            cellNote.setCellStyle(style);
            operation.setResult(zal);
        }
        return num + 1;
    }

    private static void createReportFooter(int rowNumber, XSSFSheet sheet, Operation operation) {
        Row row = sheet.createRow(rowNumber);
        Cell cell0 = row.createCell(0);
        cell0.setCellValue("Сальдо на кінець: " + round(operation.getResult(), 2));

        Row rowPrepared = sheet.createRow(rowNumber + 2);
        Cell preparedCell = rowPrepared.createCell(0);
        preparedCell.setCellValue("Склав                            _________________           Кич Я.С.");

        Row rowReview = sheet.createRow(rowNumber + 4);
        Cell reviewCell = rowReview.createCell(0);
        reviewCell.setCellValue("Перевірив                    _________________           Дунас Н.М.");

        Row rowSum = sheet.createRow(rowNumber - 1);
        Cell cell4 = rowSum.createCell(4);
        Cell cell5 = rowSum.createCell(5);
        cell4.setCellValue(round(operation.getIn(), 2));
        cell5.setCellValue(round(operation.getOut(), 2));
        operation.setIn(0.0);
        operation.setOut(0.0);
        IntStream.range(1, 8).forEach(sheet::autoSizeColumn);

        sheet.getPrintSetup().setLandscape(true);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 10);
    }

    private static CellStyle getCellStyle(Cell cellNumberOfRow) {
        CellStyle style = cellNumberOfRow.getSheet().getWorkbook().createCellStyle();
        style.setFont(cellNumberOfRow.getSheet().getWorkbook().createFont());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void setCenterInStyle(CellStyle style) {
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    private static String getDocName(HkRecord record) {
        return record.getDoc().replace("Кальк.", "Акт")
                .replace("Перемещение", "Переміщення")
                .replace("Расх. накл.", "Видаткова накладна");
    }

    private static String getPartnerForOrder(HkRecord record) {
        String document = record.getDoc();
        if (document.contains("Расх. накл. ХК-")
                || document.contains("Возвратная накл. ХК-")
                || document.contains("Прих. накл. ХК-")) {
            return ReadFromExcel.getDocRecordMap().get(document + DELIMITER + record.getDate().toString());
        }
        if (document.contains("Кальк. ХК-")) {
            return "Цех";
        }
        return "Склад";
    }
}
