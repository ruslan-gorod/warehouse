package org.hk;

import org.hk.models.HkRecord;
import org.hk.services.ReadFromExcel;
import org.hk.services.WriteToExcel;
import org.hk.util.Helper;

import java.util.List;
import java.util.Set;

public class App {
    public static void main(String[] args) {
        Helper.initMonthNames();

        List<HkRecord> records = ReadFromExcel.read();
        System.out.println("records.size = " + records.size());
        Set<String> products = ReadFromExcel.getProductValues();
        System.out.println("products.size =" + products.size());
//        WorkWithDB.writeRecords(records);

        WriteToExcel.write(products);

        System.out.println("Completed");
        System.exit(0);
    }
}
