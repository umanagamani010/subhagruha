package com.shop.controller;

import com.shop.dao.OrderDAO;
import com.shop.model.Cart;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * GET  /checkout -> shows the details form
 * POST /checkout -> validates, saves the order (one DB transaction), clears the cart
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Cart cart = (Cart) req.getSession().getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        req.getRequestDispatcher("/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        String name = trim(req.getParameter("name"));
        String phone = trim(req.getParameter("phone"));
        String email = trim(req.getParameter("email"));
        String address = trim(req.getParameter("address"));
        String payment = trim(req.getParameter("payment"));

        // Validate on the server (browser checks can be bypassed)
        String error = null;
        if (name.isEmpty() || name.length() > 100) {
            error = "Please enter your name.";
        } else if (!phone.matches("\\d{10}")) {
            error = "Phone number must be 10 digits.";
        } else if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            error = "Please enter a valid email address.";
        } else if (address.isEmpty() || address.length() > 255) {
            error = "Please enter your delivery address.";
        } else if (!"COD".equals(payment) && !"ONLINE".equals(payment)) {
            error = "Please choose a payment method.";
        }

        if (error != null) {
            req.setAttribute("error", error);
            req.getRequestDispatcher("/checkout.jsp").forward(req, resp);
            return;
        }

        try {
            int orderId = new OrderDAO().placeOrder(name, phone, email, address, payment, cart);
            cart.clear();
            if ("ONLINE".equals(payment)) {
                // the order is saved as PENDING; the customer now pays on the Razorpay page
                session.setAttribute("payOrderId", orderId);
                resp.sendRedirect(req.getContextPath() + "/pay");
                return;
            }
            session.setAttribute("lastOrderId", orderId);
            session.setAttribute("lastPayment", payment);
            resp.sendRedirect(req.getContextPath() + "/success.jsp");   // redirect so refresh doesn't re-order
        } catch (OrderDAO.OrderException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/checkout.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Could not save the order", e);
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
