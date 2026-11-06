<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.net.URLEncoder, com.shop.model.Product, com.shop.model.Review, com.shop.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= com.shop.util.ShopInfo.NAME %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main>
    <section class="hero">
        <div class="container">
            <div>
                <h1>Natural pooja essentials and moringa powder, delivered to your door</h1>
                <p>Cow dung cakes, moringa leaf powder and clay diyas. Add what you need to the cart and pay on delivery or online.</p>
                <a class="btn light" href="#products">Shop now</a>
            </div>
            <div class="art">
                <img src="<%= ctx %>/images/cowdung.svg" alt="">
                <img src="<%= ctx %>/images/diya.svg" alt="">
            </div>
        </div>
    </section>

    <div class="container" id="products">
        <% if (request.getParameter("cancelled") != null) { %>
            <div class="notice"><span>Your unpaid order was cancelled and the items were put back in stock.</span></div>
        <% } %>
        <% if (request.getParameter("added") != null) { %>
            <div class="notice"><span>Item added to your cart.</span> <a href="<%= ctx %>/cart">View cart &rarr;</a></div>
        <% } %>

        <%
            List<Product> allProducts = (List<Product>) request.getAttribute("products");
            int productCount = (allProducts == null) ? 0 : allProducts.size();
            List<String> categories = (List<String>) request.getAttribute("categories");
            String selected = (String) request.getAttribute("selectedCategory");
            if (selected == null) { selected = ""; }
        %>
        <div class="chips">
            <a class="chip <%= selected.isEmpty() ? "active" : "" %>" href="<%= ctx %>/products">All</a>
            <% if (categories != null) { for (String c : categories) { %>
                <a class="chip <%= c.equals(selected) ? "active" : "" %>"
                   href="<%= ctx %>/products?category=<%= URLEncoder.encode(c, "UTF-8") %>"><%= HtmlUtil.esc(c) %></a>
            <% } } %>
        </div>
        <div class="section-title"><h2><%= selected.isEmpty() ? "All products" : HtmlUtil.esc(selected) %></h2><span><%= productCount %> items</span></div>

        <div class="grid">
        <%
            if (allProducts != null) {
                for (Product p : allProducts) {
                    String img = p.getImageFile();
        %>
            <div class="card">
                <a class="img" href="<%= ctx %>/product?id=<%= p.getId() %>">
                    <% if (img != null && !img.isEmpty()) { %>
                        <img src="<%= ctx %>/images/<%= HtmlUtil.esc(img) %>" alt="<%= HtmlUtil.esc(p.getName()) %>">
                    <% } else { %>
                        <div class="ph"><%= HtmlUtil.esc(p.getName().substring(0, 1)) %></div>
                    <% } %>
                    <% if (p.getStock() <= 0) { %>
                        <span class="tag out">Out of stock</span>
                    <% } else if (p.getStock() <= 10) { %>
                        <span class="tag low">Only <%= p.getStock() %> left</span>
                    <% } %>
                </a>
                <div class="body">
                    <h3><a href="<%= ctx %>/product?id=<%= p.getId() %>"><%= HtmlUtil.esc(p.getName()) %></a></h3>
                    <% if (p.getReviewCount() > 0) { %>
                        <div class="rating"><span class="stars"><%= HtmlUtil.stars(p.getAvgRating()) %></span>
                            <%= String.format("%.1f", p.getAvgRating()) %> (<%= p.getReviewCount() %>)</div>
                    <% } else { %>
                        <div class="rating muted">No reviews yet</div>
                    <% } %>
                    <p class="desc"><%= HtmlUtil.esc(p.getDescription()) %></p>
                    <div class="price">&#8377;<%= p.getPrice() %></div>

                    <% if (p.getStock() > 0) { %>
                        <form action="<%= ctx %>/cart" method="post">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productId" value="<%= p.getId() %>">
                            <input type="number" name="quantity" value="1" min="1" max="<%= Math.min(p.getStock(), 20) %>" aria-label="Quantity">
                            <button type="submit">Add to Cart</button>
                        </form>
                    <% } else { %>
                        <div class="soldout">Currently unavailable</div>
                    <% } %>
                </div>
            </div>
        <%
                }
            }
        %>
        </div>

        <%
            List<Review> latestReviews = (List<Review>) request.getAttribute("latestReviews");
            if (latestReviews != null && !latestReviews.isEmpty()) {
        %>
        <section class="reviews-home">
            <div class="section-title"><h2>What our customers say</h2></div>
            <div class="review-cards">
                <% for (Review r : latestReviews) { %>
                <div class="panel review-card">
                    <div class="stars"><%= HtmlUtil.stars(r.getRating()) %></div>
                    <p>&ldquo;<%= HtmlUtil.esc(r.getText()) %>&rdquo;</p>
                    <div class="who"><strong><%= HtmlUtil.esc(r.getCustomerName()) %></strong>
                        on <a href="<%= ctx %>/product?id=<%= r.getProductId() %>#reviews"><%= HtmlUtil.esc(r.getProductName()) %></a></div>
                </div>
                <% } %>
            </div>
        </section>
        <% } %>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
