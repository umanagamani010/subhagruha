package com.shop.controller;

import com.shop.dao.ProductDAO;
import com.shop.dao.ReviewDAO;
import com.shop.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/** Shows one product with its customer reviews and the "write a review" form. */
@WebServlet("/product")
public class ProductDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        try {
            Product product = new ProductDAO().findById(id);
            if (product == null) {
                resp.sendRedirect(req.getContextPath() + "/products");
                return;
            }
            req.setAttribute("product", product);
            req.setAttribute("reviews", new ReviewDAO().findByProduct(id));
        } catch (SQLException e) {
            throw new ServletException("Could not load the product", e);
        }

        // messages and typed values saved by ReviewServlet (shown once)
        HttpSession session = req.getSession(false);
        if (session != null) {
            for (String key : new String[] { "reviewError", "reviewName", "reviewText", "reviewRating" }) {
                Object value = session.getAttribute(key);
                if (value != null) {
                    req.setAttribute(key, value);
                    session.removeAttribute(key);
                }
            }
        }
        req.getRequestDispatcher("/product.jsp").forward(req, resp);
    }
}
