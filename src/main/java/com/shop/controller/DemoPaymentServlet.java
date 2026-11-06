package com.shop.controller;

import com.shop.dao.OrderDAO;
import com.shop.payment.RazorpayConfig;
import com.shop.util.ShopInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * DEMO payment (no real money). It works only when demo payments are allowed in ShopInfo
 * AND no Razorpay keys are set, so it can never replace a real payment on a real shop.
 */
@WebServlet("/payment/demo")
public class DemoPaymentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String ctx = req.getContextPath();
        if (!ShopInfo.ALLOW_DEMO_PAYMENT || RazorpayConfig.isConfigured()) {
            resp.sendRedirect(ctx + "/pay");
            return;
        }

        HttpSession session = req.getSession(false);
        Integer orderId = session == null ? null : (Integer) session.getAttribute("payOrderId");
        if (orderId == null) {
            resp.sendRedirect(ctx + "/products");
            return;
        }

        if (!"success".equals(req.getParameter("result"))) {
            resp.sendRedirect(ctx + "/pay?demoFailed=1");     // pretend the payment failed; the order stays pending
            return;
        }

        try {
            if (new OrderDAO().markPaidDemo(orderId)) {
                session.removeAttribute("payOrderId");
                session.setAttribute("lastOrderId", orderId);
                session.setAttribute("lastPayment", "ONLINE");
                session.setAttribute("lastPaid", Boolean.TRUE);
                session.setAttribute("lastDemo", Boolean.TRUE);
                resp.sendRedirect(ctx + "/success.jsp");
            } else {
                resp.sendRedirect(ctx + "/products");
            }
        } catch (SQLException e) {
            throw new ServletException("Could not complete the demo payment", e);
        }
    }
}
