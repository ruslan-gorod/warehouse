package org.hk;


import org.hk.dao.WorkWithDB;

public class App {
    public static void main(String[] args) {
        System.out.println(WorkWithDB.getYearFromDB("MIN"));
        System.out.println(WorkWithDB.getYearFromDB("MAX"));
//        Helper.initMonthNames();
//
//        List<HkRecord> records = ReadFromExcel.read();
//        System.out.println("records.size =" + records.size());
//        WorkWithDB.writeRecords(records);
//
//        Set<String> products = ReadFromExcel.getProductValues();
//        System.out.println("products.size =" + products.size());
//
//        WriteToExcel.write(products);
//
//        System.out.println("Completed");
//        System.exit(0);
    }
}
