package org.hk.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hk.models.HkRecord;
import org.hk.util.HibernateUtil;

import java.time.LocalDate;
import java.util.List;

public class WorkWithDB {
    public static void writeRecords(List<HkRecord> records) {
        for (HkRecord record : records) {
            Transaction transaction = null;
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

    public static LocalDate getDateFromDB(String value) {
        LocalDate date = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.setDefaultReadOnly(true);
            String query = "SELECT " + value + "(r.date) FROM HkRecord r";
            List results = session.createQuery(query).list();

            if (results != null && !results.isEmpty()) {
                date = (LocalDate) results.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static List<HkRecord> getReportFromDb(Session session, int month, int year, String product) {
        Query query = session.createQuery(
                "FROM HkRecord r WHERE EXTRACT(MONTH FROM r.date) = :month " +
                        "AND EXTRACT(YEAR FROM r.date) = :year " +
                        "AND r.product LIKE :product " +
                        "ORDER BY r.dateTime");
        query.setParameter("month", month);
        query.setParameter("year", year);
        query.setParameter("product", product);

        return query.list();

    }
}
