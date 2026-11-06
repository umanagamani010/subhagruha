<%-- Top strip + header bar, included by every page. The shop name comes from com.shop.util.ShopInfo. --%>
<%@ page import="com.shop.model.Cart, com.shop.util.ShopInfo" %>
<%
    Cart headerCart = (Cart) session.getAttribute("cart");
    int headerCount = (headerCart == null) ? 0 : headerCart.getCount();
    String ctx = request.getContextPath();
%>
<div class="strip">Cash on Delivery available on all orders</div>
<header class="header">
    <div class="container">
        <a class="brand" href="<%= ctx %>/products"><span class="logo"><%= ShopInfo.NAME.substring(0, 1).toUpperCase() %></span> <%= ShopInfo.NAME %></a>
        <nav class="nav">
            <a href="<%= ctx %>/products">Shop</a>
            <a href="<%= ctx %>/contact.jsp">Contact</a>
            <a class="cart-btn" href="<%= ctx %>/cart">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/><path d="M2 3h3l2.7 12.4a2 2 0 0 0 2 1.6h7.7a2 2 0 0 0 2-1.5L21 8H6"/></svg>
                View Cart
                <span class="badge"><%= headerCount %></span>
            </a>
        </nav>
    </div>
</header>
