package com.hcmute.clinic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class FindUnpaidInvoice {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/phongkham";
        String user = "postgres";
        String password = "123";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT id, patient_id, total_amount, payment_status FROM invoices WHERE payment_status != 'PAID' ORDER BY id DESC LIMIT 1");
            if (rs.next()) {
                System.out.println("FOUND_INVOICE_ID:" + rs.getLong("id"));
                System.out.println("FOUND_PATIENT_ID:" + rs.getLong("patient_id"));
                System.out.println("FOUND_AMOUNT:" + rs.getDouble("total_amount"));
            } else {
                System.out.println("NO_UNPAID_INVOICES_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
