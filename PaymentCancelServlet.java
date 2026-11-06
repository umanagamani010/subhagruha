package com.shop.controller;

import com.shop.dao.OrderDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/** Cancels an unpaid online order and puts the items back in stock. */
@WebServlet("/payment/cancel")
public class PaymentCancelServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Integer orderId = session == null ? null : (Integer) session.getAttribute("payOrderId");
        if (orderId != null) {
            try {
                new OrderDAO().cancelUnpaidOnlineOrder(orderId);
            } catch (SQLException e) {
                throw new ServletException("Could not cancel the order", e);
            }
            session.removeAttribute("payOrderId");
        }
        resp.sendRedirect(req.getContextPath() + "/products?cancelled=1");
    }
}
