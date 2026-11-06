package com.shop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Runs once when the website starts. It adds anything the newer features need if it is missing
 * (the reviews table and the Razorpay columns), so old databases keep working.
 * It never deletes or changes existing data.
 */
@WebListener
public class SchemaUpdater implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection con = DBUtil.getConnection(); Statement st = con.createStatement()) {

            st.executeUpdate("CREATE TABLE IF NOT EXISTS reviews ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "product_id INT NOT NULL, "
                    + "customer_name VARCHAR(60) NOT NULL, "
                    + "rating TINYINT NOT NULL, "
                    + "review_text VARCHAR(500) NOT NULL, "
                    + "visible TINYINT(1) NOT NULL DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "INDEX idx_reviews_product (product_id), "
                    + "FOREIGN KEY (product_id) REFERENCES products(id))");

            addColumnIfMissing(con, "orders", "razorpay_order_id", "VARCHAR(40)");
            addColumnIfMissing(con, "orders", "razorpay_payment_id", "VARCHAR(40)");

        } catch (Exception e) {
            // the website still starts; the error shows when the database is really needed
            System.err.println("Database check skipped: " + e.getMessage());
        }
    }

    private void addColumnIfMissing(Connection con, String table, String column, String type) throws SQLException {
        String check = "SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    try (Statement st = con.createStatement()) {
                        st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
                    }
                }
            }
        }
    }
}
