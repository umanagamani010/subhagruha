package com.shop.dao;

import com.shop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Repository class: reads products (with their average rating) from the database. */
public class ProductDAO {

    // products + the average rating and number of visible reviews of each product
    private static final String SELECT =
            "SELECT p.id, p.name, p.description, p.price, p.stock, p.category, p.image_file, "
          + "COALESCE(r.avg_rating, 0) AS avg_rating, COALESCE(r.cnt, 0) AS review_count "
          + "FROM products p LEFT JOIN ("
          + "   SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS cnt "
          + "   FROM reviews WHERE visible = 1 GROUP BY product_id) r ON r.product_id = p.id";

    public List<Product> findAll() throws SQLException {
        return findByCategory(null);
    }

    /** All products, or only one category when a category name is given. */
    public List<Product> findByCategory(String category) throws SQLException {
        List<Product> list = new ArrayList<>();
        boolean all = (category == null || category.isEmpty());
        String sql = SELECT + (all ? "" : " WHERE p.category = ?") + " ORDER BY p.id";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (!all) {
                ps.setString(1, category);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** The category names, used for the filter buttons. */
    public List<String> findCategories() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products ORDER BY category";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        }
        return list;
    }

    public Product findById(int id) throws SQLException {
        String sql = SELECT + " WHERE p.id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setCategory(rs.getString("category"));
        p.setImageFile(rs.getString("image_file"));
        p.setAvgRating(rs.getDouble("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));
        return p;
    }
}
