package com.shop.dao;

import com.shop.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Repository class: saves and reads customer reviews. */
public class ReviewDAO {

    public void add(int productId, String customerName, int rating, String text) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, customer_name, rating, review_text) VALUES (?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, customerName);
            ps.setInt(3, rating);
            ps.setString(4, text);
            ps.executeUpdate();
        }
    }

    /** Visible reviews of one product, newest first. */
    public List<Review> findByProduct(int productId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT id, product_id, customer_name, rating, review_text, created_at "
                + "FROM reviews WHERE product_id = ? AND visible = 1 ORDER BY created_at DESC, id DESC";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs, null));
                }
            }
        }
        return list;
    }

    /** The newest visible reviews of the whole shop (for the home page). */
    public List<Review> latest(int limit) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.id, r.product_id, r.customer_name, r.rating, r.review_text, r.created_at, p.name AS product_name "
                + "FROM reviews r JOIN products p ON p.id = r.product_id "
                + "WHERE r.visible = 1 ORDER BY r.created_at DESC, r.id DESC LIMIT ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs, rs.getString("product_name")));
                }
            }
        }
        return list;
    }

    private Review map(ResultSet rs, String productName) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setProductId(rs.getInt("product_id"));
        r.setProductName(productName);
        r.setCustomerName(rs.getString("customer_name"));
        r.setRating(rs.getInt("rating"));
        r.setText(rs.getString("review_text"));
        Timestamp t = rs.getTimestamp("created_at");
        r.setCreatedAt(t == null ? null : new java.util.Date(t.getTime()));
        return r;
    }
}
