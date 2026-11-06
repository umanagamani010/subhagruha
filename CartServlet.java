package com.shop.controller;

import com.shop.dao.ProductDAO;
import com.shop.model.Cart;
import com.shop.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * GET  /cart  -> shows the cart page (the separate cart page)
 * POST /cart  -> action = add | update | remove
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }

        String action = req.getParameter("action");
        int productId = parseInt(req.getParameter("productId"), 0);
        int quantity = parseInt(req.getParameter("quantity"), 1);

        try {
            if ("add".equals(action)) {
                Product p = new ProductDAO().findById(productId);
                if (p != null && p.getStock() > 0 && quantity > 0) {
                    cart.add(p, quantity);
                }
                resp.sendRedirect(req.getContextPath() + "/products?added=1");
                return;
            } else if ("update".equals(action)) {
                cart.update(productId, quantity);
            } else if ("remove".equals(action)) {
                cart.remove(productId);
            }
        } catch (SQLException e) {
            throw new ServletException("Cart error", e);
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
