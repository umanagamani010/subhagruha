package com.shop.controller;

import com.shop.dao.ProductDAO;
import com.shop.dao.ReviewDAO;
import com.shop.model.Review;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Shows the product list page. */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String category = req.getParameter("category");
        try {
            ProductDAO dao = new ProductDAO();
            req.setAttribute("products", dao.findByCategory(category));
            req.setAttribute("categories", dao.findCategories());
            req.setAttribute("selectedCategory", category == null ? "" : category);

            // newest customer reviews for the "What our customers say" section (the page works without them)
            List<Review> latest = new ArrayList<>();
            try {
                latest = new ReviewDAO().latest(3);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            req.setAttribute("latestReviews", latest);
        } catch (SQLException e) {
            throw new ServletException("Could not load products", e);
        }
        req.getRequestDispatcher("/products.jsp").forward(req, resp);
    }
}
