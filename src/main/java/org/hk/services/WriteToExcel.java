package org.hk.services;

import java.util.Set;

public class WriteToExcel {

    public static void write(Set<String> products) {
        int startMonth;
        int startYear;
        int endYear;

        for (String product : products) {

        }
    }

//    private static void createAndSaveFile(Integer year, Integer month, String dirForOrders, Map<String, List<Record>> mapProdRecord, String prod) {
//        String name = dirForOrders + "/" + prod.replace('/', ' ') + ".xlsx";
//        try {
//            File file = new File(name);
//            FileOutputStream fos = new FileOutputStream(file);
//            XSSFWorkbook workbook = new XSSFWorkbook();
//            XSSFSheet sheet = workbook.createSheet(dir);
//            createHeader(sheet, prod, month, year);
//            int rowNumber = 8;
//            sumPryhid = 0.0;
//            sumRozhid = 0.0;
//            for (Record r : mapProdRecord.get(prod)) {
//                addRowToOrder(r, sheet, rowNumber++);
//            }
//            createFooter(rowNumber + 1, prod, sheet);
//            workbook.write(fos);
//            fos.flush();
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
