package org.hk.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hk.models.HkRecord;
import org.hk.util.HibernateUtil;

import java.util.List;

public class WorkWithDB {
    public static void writeRecords(List<HkRecord> records) {
        Transaction transaction = null;
        for (HkRecord record : records) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();
                session.save(record);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
            }
        }
    }

    public static int getYearFromDB(String value) {
        int year = 0;
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query query = session.createQuery("SELECT " + value + "(EXTRACT(YEAR FROM date)) FROM record");
            List results = query.list();

            if (results != null && !results.isEmpty()) {
                year = ((Number) results.get(0)).intValue();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return year;
    }
}
