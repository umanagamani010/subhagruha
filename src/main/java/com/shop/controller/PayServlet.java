package com.shop.controller;

import com.shop.dao.OrderDAO;
import com.shop.model.Order;
import com.shop.payment.RazorpayClient;
import com.shop.payment.RazorpayConfig;
import com.shop.util.ShopInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.SQLException;

/** Shows the "Pay now" page: creates the Razorpay order on the server, then the page opens Razorpay Checkout. */
@WebServlet("/pay")
public class PayServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Integer orderId = session == null ? null : (Integer) session.getAttribute("payOrderId");
        if (orderId == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        try {
            OrderDAO dao = new OrderDAO();
            Order order = dao.findById(orderId);
            if (order == null || !"ONLINE".equals(order.getPaymentMethod())
                    || !"PENDING".equals(order.getPaymentStatus())) {
                session.removeAttribute("payOrderId");
                resp.sendRedirect(req.getContextPath() + "/products");
                return;
            }
            req.setAttribute("order", order);

            if (!RazorpayConfig.isConfigured()) {
                // no Razorpay keys: use the demo payment page when it is allowed
                req.setAttribute(ShopInfo.ALLOW_DEMO_PAYMENT ? "demoMode" : "notConfigured", Boolean.TRUE);
            } else {
                // the amount always comes from OUR database, never from the browser (Rs. 360.00 -> 36000 paise)
                long paise = order.getTotal().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
                String razorpayOrderId = order.getRazorpayOrderId();
                try {
                    if (razorpayOrderId == null || razorpayOrderId.isEmpty()) {
                        razorpayOrderId = RazorpayClient.createOrder(paise, "order_" + order.getId());
                        dao.saveRazorpayOrderId(order.getId(), razorpayOrderId);
                    }
                    req.setAttribute("keyId", RazorpayConfig.keyId());
                    req.setAttribute("rzpOrderId", razorpayOrderId);
                    req.setAttribute("amountPaise", paise);
                } catch (IOException e) {
                    req.setAttribute("payError", e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Could not load the order", e);
        }
        req.getRequestDispatcher("/pay.jsp").forward(req, resp);
    }
}
