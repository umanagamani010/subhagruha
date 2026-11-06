<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.shop.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contact us - <%= com.shop.util.ShopInfo.NAME %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="container">
    <h1 class="page-title">Contact us</h1>
    <p class="lead">Questions about a product or your order? We are happy to help.</p>

    <div class="contact-grid">
        <div class="panel contact-card">
            <h3>Call us</h3>
            <p><a class="big-link" href="tel:<%= HtmlUtil.esc(ShopInfo.PHONE_DIAL) %>"><%= HtmlUtil.esc(ShopInfo.PHONE_DISPLAY) %></a></p>
            <p class="muted"><%= HtmlUtil.esc(ShopInfo.HOURS) %></p>
        </div>

        <div class="panel contact-card">
            <h3>WhatsApp</h3>
            <p>Send us a message and we will reply as soon as we can.</p>
            <a class="btn" href="https://wa.me/<%= HtmlUtil.esc(ShopInfo.WHATSAPP) %>?text=Hello%20<%= java.net.URLEncoder.encode(ShopInfo.NAME, "UTF-8") %>"
               target="_blank" rel="noopener">Chat on WhatsApp</a>
        </div>

        <div class="panel contact-card">
            <h3>Email</h3>
            <p><a class="big-link" href="mailto:<%= HtmlUtil.esc(ShopInfo.EMAIL) %>"><%= HtmlUtil.esc(ShopInfo.EMAIL) %></a></p>
            <p class="muted">We usually reply within one working day.</p>
        </div>

        <div class="panel contact-card">
            <h3>Visit us</h3>
            <p><%= HtmlUtil.esc(ShopInfo.ADDRESS) %></p>
            <p class="muted"><%= HtmlUtil.esc(ShopInfo.HOURS) %></p>
        </div>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
