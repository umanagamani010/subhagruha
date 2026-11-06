<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Integer orderId = (Integer) session.getAttribute("lastOrderId");
    String payment = (String) session.getAttribute("lastPayment");
    boolean paid = Boolean.TRUE.equals(session.getAttribute("lastPaid"));
    boolean demo = Boolean.TRUE.equals(session.getAttribute("lastDemo"));
    if (orderId == null) {                       // opened directly: nothing to show
        response.sendRedirect(request.getContextPath() + "/products");
        return;
    }
    session.removeAttribute("lastOrderId");      // show the message only once
    session.removeAttribute("lastPayment");
    session.removeAttribute("lastPaid");
    session.removeAttribute("lastDemo");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order placed - <%= com.shop.util.ShopInfo.NAME %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="container">
    <div class="panel success">
        <div class="tick">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M5 13l4 4L19 7"/></svg>
        </div>
        <h1>Thank you! Your order is placed.</h1>
        <div class="order-no">Order number <strong>#<%= orderId %></strong></div>
        <% if ("ONLINE".equals(payment) && paid) { %>
            <p>Payment received. Payment status: <strong>PAID</strong>.</p>
            <% if (demo) { %><p class="muted">This was a demo payment, so no real money was used.</p><% } %>
        <% } else if ("ONLINE".equals(payment)) { %>
            <p>Payment status: <strong>pending</strong>.</p>
        <% } else { %>
            <p>Please keep the amount ready. You will pay when the order is delivered.</p>
        <% } %>
        <p><a class="btn" href="<%= ctx %>/products">Continue shopping</a></p>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
