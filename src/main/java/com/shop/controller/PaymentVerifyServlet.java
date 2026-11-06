package com.shop.controller;

import com.shop.dao.OrderDAO;
import com.shop.model.Order;
import com.shop.payment.RazorpayClient;
import com.shop.payment.RazorpayConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Razorpay Checkout sends payment id + signature here after the customer paid.
 * The order is marked PAID only when the signature is correct.
 */
@WebServlet("/payment/verify")
public class PaymentVerifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Integer orderId = session == null ? null : (Integer) session.getAttribute("payOrderId");
        if (orderId == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        String paymentId = req.getParameter("razorpay_payment_id");
        String signature = req.getParameter("razorpay_signature");

        try {
            OrderDAO dao = new OrderDAO();
            Order order = dao.findById(orderId);

            // use the Razorpay order id stored on OUR server, not the one sent by the browser
            boolean ok = order != null
                    && order.getRazorpayOrderId() != null
                    && RazorpayClient.verifySignature(order.getRazorpayOrderId(), paymentId, signature,
                            RazorpayConfig.keySecret())
                    && dao.markPaid(orderId, order.getRazorpayOrderId(), paymentId);

            if (ok) {
                session.removeAttribute("payOrderId");
                session.setAttribute("lastOrderId", orderId);
                session.setAttribute("lastPayment", "ONLINE");
                session.setAttribute("lastPaid", Boolean.TRUE);
                resp.sendRedirect(req.getContextPath() + "/success.jsp");
            } else {
                resp.sendRedirect(req.getContextPath() + "/pay?failed=1");
            }
        } catch (SQLException e) {
            throw new ServletException("Could not confirm the payment", e);
        }
    }
}
