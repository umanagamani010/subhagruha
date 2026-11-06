<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.shop.model.Cart, com.shop.model.CartItem, com.shop.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout - <%= com.shop.util.ShopInfo.NAME %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="container">
    <h1 class="page-title">Checkout</h1>

    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
        <div class="error"><%= HtmlUtil.esc(error) %></div>
    <% } %>

    <%
        Cart cart = (Cart) session.getAttribute("cart");
        String payment = request.getParameter("payment");
    %>

    <form action="<%= ctx %>/checkout" method="post">
    <div class="two-col">
        <div class="panel">
            <h3>Delivery details</h3>
            <label class="field">Full name
                <input type="text" name="name" maxlength="100" required placeholder="e.g. Uma Nagamani"
                       value="<%= HtmlUtil.esc(request.getParameter("name")) %>">
            </label>
            <div class="row2">
                <label class="field">Phone number
                    <input type="tel" name="phone" pattern="\d{10}" required placeholder="10 digits"
                           value="<%= HtmlUtil.esc(request.getParameter("phone")) %>">
                </label>
                <label class="field">Email <span class="hint">(optional)</span>
                    <input type="email" name="email" maxlength="100" placeholder="you@example.com"
                           value="<%= HtmlUtil.esc(request.getParameter("email")) %>">
                </label>
            </div>
            <label class="field">Delivery address
                <textarea name="address" rows="3" maxlength="255" required placeholder="House no, street, city, PIN code"><%= HtmlUtil.esc(request.getParameter("address")) %></textarea>
            </label>

            <h3 style="margin-top:24px">Payment method</h3>
            <label class="pay">
                <input type="radio" name="payment" value="COD" <%= (payment == null || "COD".equals(payment)) ? "checked" : "" %>>
                <span><b>Cash on Delivery</b><small>Pay in cash when your order arrives</small></span>
            </label>
            <label class="pay">
                <input type="radio" name="payment" value="ONLINE" <%= "ONLINE".equals(payment) ? "checked" : "" %>>
                <span><b>Pay online (UPI / card)</b><small><%= com.shop.payment.RazorpayConfig.isConfigured() ? "Pay with UPI, card or netbanking on the secure Razorpay page" : "Demo payment: a pretend UPI screen, no real money is used" %></small></span>
            </label>
        </div>

        <aside class="panel summary mini">
            <h3>Your order</h3>
            <% for (CartItem item : cart.getItems()) { %>
                <div class="row">
                    <span><%= HtmlUtil.esc(item.getProduct().getName()) %> &times; <%= item.getQuantity() %></span>
                    <span>&#8377;<%= item.getSubtotal() %></span>
                </div>
            <% } %>
            <div class="row total"><span>Total</span><span>&#8377;<%= cart.getTotal() %></span></div>
            <button type="submit" class="btn block">Place Order</button>
            <p class="note"><a href="<%= ctx %>/cart">&larr; Back to cart</a></p>
        </aside>
    </div>
    </form>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
