package com.shop.controller;

import com.shop.dao.ProductDAO;
import com.shop.dao.ReviewDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/** Saves a customer review. Validated on the server; one review per product per visit. */
@WebServlet("/review")
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAX_REVIEWS_PER_VISIT = 5;

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();

        int productId;
        try {
            productId = Integer.parseInt(req.getParameter("productId"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(ctx + "/products");
            return;
        }
        String back = ctx + "/product?id=" + productId + "#reviews";

        // hidden "website" field: real visitors never fill it, spam bots usually do
        if (!trim(req.getParameter("website")).isEmpty()) {
            resp.sendRedirect(back);
            return;
        }

        String name = trim(req.getParameter("name"));
        String text = trim(req.getParameter("text"));
        int rating = 0;
        try {
            rating = Integer.parseInt(trim(req.getParameter("rating")));
        } catch (NumberFormatException e) {
            // stays 0 and is rejected below
        }

        HttpSession session = req.getSession();
        Set<Integer> done = (Set<Integer>) session.getAttribute("reviewedProducts");
        if (done == null) {
            done = new HashSet<>();
            session.setAttribute("reviewedProducts", done);
        }

        String error = null;
        if (name.length() < 2 || name.length() > 60) {
            error = "Please enter your name (2 to 60 characters).";
        } else if (rating < 1 || rating > 5) {
            error = "Please choose a rating from 1 to 5.";
        } else if (text.length() < 5 || text.length() > 500) {
            error = "Please write your review (5 to 500 characters).";
        } else if (done.contains(productId)) {
            error = "You have already reviewed this product. Thank you!";
        } else if (done.size() >= MAX_REVIEWS_PER_VISIT) {
            error = "You have reached the review limit for now. Thank you!";
        }

        try {
            if (error == null && new ProductDAO().findById(productId) == null) {
                resp.sendRedirect(ctx + "/products");
                return;
            }
            if (error == null) {
                new ReviewDAO().add(productId, name, rating, text);
                done.add(productId);
                resp.sendRedirect(ctx + "/product?id=" + productId + "&reviewed=1#reviews");
                return;
            }
        } catch (SQLException e) {
            throw new ServletException("Could not save the review", e);
        }

        // keep what the customer typed and show the problem on the product page
        session.setAttribute("reviewError", error);
        session.setAttribute("reviewName", name);
        session.setAttribute("reviewText", text);
        session.setAttribute("reviewRating", String.valueOf(rating));
        resp.sendRedirect(back);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
