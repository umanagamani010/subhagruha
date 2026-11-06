<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.shop.model.Cart, com.shop.model.CartItem, com.shop.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your Cart - <%= com.shop.util.ShopInfo.NAME %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="container">
    <h1 class="page-title">Shopping Cart</h1>
    <%
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
    %>
        <div class="panel empty">
            <div class="big">&#128722;</div>
            <h2>Your cart is empty</h2>
            <p>Looks like you haven't added anything yet.</p>
            <a class="btn" href="<%= ctx %>/products">Start shopping</a>
        </div>
    <% } else { %>
        <div class="two-col">
            <div class="panel">
                <% for (CartItem item : cart.getItems()) {
                       String img = item.getProduct().getImageFile(); %>
                <div class="line">
                    <div class="thumb">
                        <% if (img != null && !img.isEmpty()) { %>
                            <img src="<%= ctx %>/images/<%= HtmlUtil.esc(img) %>" alt="">
                        <% } else { %>
                            <%= HtmlUtil.esc(item.getProduct().getName().substring(0, 1)) %>
                        <% } %>
                    </div>
                    <div>
                        <div class="name"><%= HtmlUtil.esc(item.getProduct().getName()) %></div>
                        <div class="unit">&#8377;<%= item.getProduct().getPrice() %> each</div>
                        <div class="controls">
                            <form action="<%= ctx %>/cart" method="post">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                                <input type="number" name="quantity" value="<%= item.getQuantity() %>" min="0" max="20" aria-label="Quantity">
                                <button type="submit" class="small ghost btn">Update</button>
                            </form>
                            <form action="<%= ctx %>/cart" method="post">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                                <button type="submit" class="link">Remove</button>
                            </form>
                        </div>
                    </div>
                    <div class="sub">&#8377;<%= item.getSubtotal() %></div>
                </div>
                <% } %>
            </div>

            <aside class="panel summary">
                <h3>Order summary</h3>
                <div class="row"><span>Items</span><span><%= cart.getCount() %></span></div>
                <div class="row total"><span>Total</span><span>&#8377;<%= cart.getTotal() %></span></div>
                <a class="btn block" href="<%= ctx %>/checkout">Proceed to Checkout</a>
                <p class="note"><a href="<%= ctx %>/products">&larr; Continue shopping</a></p>
            </aside>
        </div>
    <% } %>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
