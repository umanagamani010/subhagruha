package com.shop.dao;

import com.shop.model.Cart;
import com.shop.model.CartItem;
import com.shop.model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves an order. Everything happens in ONE database transaction:
 * either the order, its items and the stock update all succeed (commit),
 * or nothing is saved (rollback).
 */
public class OrderDAO {

    /** Thrown for problems the customer should see (for example out of stock). */
    public static class OrderException extends Exception {
        private static final long serialVersionUID = 1L;
        public OrderException(String message) { super(message); }
    }

    private static class Line {
        final int productId;
        final int quantity;
        final BigDecimal unitPrice;
        Line(int productId, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    public int placeOrder(String name, String phone, String email, String address,
                          String paymentMethod, Cart cart) throws OrderException, SQLException {

        if (cart == null || cart.isEmpty()) {
            throw new OrderException("Your cart is empty.");
        }

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);                       // start the transaction
            try {
                List<Line> lines = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                for (CartItem item : cart.getItems()) {
                    int productId = item.getProduct().getId();

                    // 1. Read the REAL price from the database (never trust the browser)
                    BigDecimal price;
                    String productName;
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT name, price FROM products WHERE id = ?")) {
                        ps.setInt(1, productId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new OrderException("A product in your cart is no longer available.");
                            }
                            productName = rs.getString("name");
                            price = rs.getBigDecimal("price");
                        }
                    }

                    // 2. Reduce stock, but only if enough is left
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?")) {
                        ps.setInt(1, item.getQuantity());
                        ps.setInt(2, productId);
                        ps.setInt(3, item.getQuantity());
                        if (ps.executeUpdate() == 0) {
                            throw new OrderException("Sorry, not enough stock for " + productName + ".");
                        }
                    }

                    lines.add(new Line(productId, item.getQuantity(), price));
                    total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
                }

                // 3. Insert the order
                int orderId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO orders (customer_name, phone, email, address, total, payment_method, payment_status) "
                                + "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, phone);
                    ps.setString(3, email);
                    ps.setString(4, address);
                    ps.setBigDecimal(5, total);
                    ps.setString(6, paymentMethod);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        orderId = keys.getInt(1);
                    }
                }

                // 4. Insert the order items
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                    for (Line line : lines) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, line.productId);
                        ps.setInt(3, line.quantity);
                        ps.setBigDecimal(4, line.unitPrice);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();                               // everything worked: save it
                return orderId;

            } catch (OrderException | SQLException | RuntimeException e) {
                con.rollback();                             // something failed: undo everything
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    //  Online payment helpers
    // ------------------------------------------------------------------

    public Order findById(int id) throws SQLException {
        String sql = "SELECT id, customer_name, phone, email, total, payment_method, payment_status, razorpay_order_id "
                + "FROM orders WHERE id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setCustomerName(rs.getString("customer_name"));
                o.setPhone(rs.getString("phone"));
                o.setEmail(rs.getString("email"));
                o.setTotal(rs.getBigDecimal("total"));
                o.setPaymentMethod(rs.getString("payment_method"));
                o.setPaymentStatus(rs.getString("payment_status"));
                o.setRazorpayOrderId(rs.getString("razorpay_order_id"));
                return o;
            }
        }
    }

    /** Remembers which Razorpay order belongs to our order. */
    public void saveRazorpayOrderId(int orderId, String razorpayOrderId) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE orders SET razorpay_order_id = ? WHERE id = ?")) {
            ps.setString(1, razorpayOrderId);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    /** Marks an online order as PAID (only if it is still PENDING and the Razorpay order matches). */
    public boolean markPaid(int orderId, String razorpayOrderId, String razorpayPaymentId) throws SQLException {
        String sql = "UPDATE orders SET payment_status = 'PAID', razorpay_payment_id = ? "
                + "WHERE id = ? AND razorpay_order_id = ? AND payment_method = 'ONLINE' AND payment_status = 'PENDING'";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, razorpayPaymentId);
            ps.setInt(2, orderId);
            ps.setString(3, razorpayOrderId);
            return ps.executeUpdate() == 1;
        }
    }

    /** DEMO payment: marks an online order as PAID without a payment gateway (no real money). */
    public boolean markPaidDemo(int orderId) throws SQLException {
        String sql = "UPDATE orders SET payment_status = 'PAID', "
                + "razorpay_order_id = COALESCE(razorpay_order_id, CONCAT('demo_order_', id)), "
                + "razorpay_payment_id = CONCAT('demo_pay_', id) "
                + "WHERE id = ? AND payment_method = 'ONLINE' AND payment_status = 'PENDING'";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Cancels an unpaid online order and puts the stock back. One transaction:
     * the status change and the stock update both happen, or neither.
     */
    public boolean cancelUnpaidOnlineOrder(int orderId) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                int changed;
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE orders SET payment_status = 'CANCELLED' "
                                + "WHERE id = ? AND payment_method = 'ONLINE' AND payment_status = 'PENDING'")) {
                    ps.setInt(1, orderId);
                    changed = ps.executeUpdate();
                }
                if (changed == 0) {
                    con.rollback();
                    return false;
                }

                List<int[]> lines = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT product_id, quantity FROM order_items WHERE order_id = ?")) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            lines.add(new int[] { rs.getInt("product_id"), rs.getInt("quantity") });
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE products SET stock = stock + ? WHERE id = ?")) {
                    for (int[] line : lines) {
                        ps.setInt(1, line[1]);
                        ps.setInt(2, line[0]);
                        ps.executeUpdate();
                    }
                }
                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
