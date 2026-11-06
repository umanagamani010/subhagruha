<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, java.net.URLEncoder, com.shop.model.Product, com.shop.model.Review, com.shop.util.HtmlUtil" %>
<%
    Product p = (Product) request.getAttribute("product");
    List<Review> reviews = (List<Review>) request.getAttribute("reviews");
    String reviewError = (String) request.getAttribute("reviewError");
    String typedName = (String) request.getAttribute("reviewName");
    String typedText = (String) request.getAttribute("reviewText");
    String typedRating = (String) request.getAttribute("reviewRating");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    String img = p.getImageFile();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= HtmlUtil.esc(p.getName()) %> - <%= com.shop.util.ShopInfo.NAME %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="container">
    <p class="crumbs">
        <a href="<%= ctx %>/products">Shop</a> &rsaquo;
        <a href="<%= ctx %>/products?category=<%= URLEncoder.encode(p.getCategory(), "UTF-8") %>"><%= HtmlUtil.esc(p.getCategory()) %></a> &rsaquo;
        <%= HtmlUtil.esc(p.getName()) %>
    </p>

    <div class="detail">
        <div class="detail-img">
            <% if (img != null && !img.isEmpty()) { %>
                <img src="<%= ctx %>/images/<%= HtmlUtil.esc(img) %>" alt="<%= HtmlUtil.esc(p.getName()) %>">
            <% } else { %>
                <div class="ph"><%= HtmlUtil.esc(p.getName().substring(0, 1)) %></div>
            <% } %>
        </div>

        <div class="detail-info">
            <h1><%= HtmlUtil.esc(p.getName()) %></h1>
            <% if (p.getReviewCount() > 0) { %>
                <a class="rating" href="#reviews"><span class="stars"><%= HtmlUtil.stars(p.getAvgRating()) %></span>
                    <%= String.format("%.1f", p.getAvgRating()) %> (<%= p.getReviewCount() %> review<%= p.getReviewCount() == 1 ? "" : "s" %>)</a>
            <% } else { %>
                <a class="rating muted" href="#reviews">No reviews yet</a>
            <% } %>
            <div class="price big">&#8377;<%= p.getPrice() %></div>
            <p class="detail-desc"><%= HtmlUtil.esc(p.getDescription()) %></p>

            <% if (p.getStock() > 0) { %>
                <% if (p.getStock() <= 10) { %><p class="low-note">Only <%= p.getStock() %> left in stock</p><% } %>
                <form action="<%= ctx %>/cart" method="post" class="buy">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="productId" value="<%= p.getId() %>">
                    <input type="number" name="quantity" value="1" min="1" max="<%= Math.min(p.getStock(), 20) %>" aria-label="Quantity">
                    <button type="submit">Add to Cart</button>
                </form>
            <% } else { %>
                <div class="soldout">Currently unavailable</div>
            <% } %>
            <p class="muted small-text">Questions about this product? <a href="<%= ctx %>/contact.jsp">Contact us</a>.</p>
        </div>
    </div>

    <section id="reviews" class="panel reviews">
        <h2>Customer reviews</h2>

        <% if (p.getReviewCount() > 0) { %>
            <p class="summary"><span class="stars big"><%= HtmlUtil.stars(p.getAvgRating()) %></span>
                <strong><%= String.format("%.1f", p.getAvgRating()) %></strong> out of 5
                &middot; <%= p.getReviewCount() %> review<%= p.getReviewCount() == 1 ? "" : "s" %></p>
        <% } else { %>
            <p class="muted">No reviews yet. Be the first to review this product.</p>
        <% } %>

        <% if (request.getParameter("reviewed") != null) { %>
            <div class="notice"><span>Thank you! Your review was added.</span></div>
        <% } %>

        <% if (reviews != null) { for (Review r : reviews) { %>
            <div class="review">
                <div class="head"><span class="stars"><%= HtmlUtil.stars(r.getRating()) %></span>
                    <strong><%= HtmlUtil.esc(r.getCustomerName()) %></strong>
                    <% if (r.getCreatedAt() != null) { %><span class="muted"><%= dateFormat.format(r.getCreatedAt()) %></span><% } %></div>
                <p><%= HtmlUtil.esc(r.getText()) %></p>
            </div>
        <% } } %>

        <h3 class="write">Write a review</h3>
        <% if (reviewError != null) { %>
            <div class="error" role="alert"><%= HtmlUtil.esc(reviewError) %></div>
        <% } %>
        <form action="<%= ctx %>/review" method="post" class="review-form">
            <input type="hidden" name="productId" value="<%= p.getId() %>">
            <div class="hp" aria-hidden="true"><label>Website <input type="text" name="website" tabindex="-1" autocomplete="off"></label></div>

            <div class="row2">
                <label class="field">Your name
                    <input type="text" name="name" minlength="2" maxlength="60" required value="<%= HtmlUtil.esc(typedName) %>">
                </label>
                <label class="field">Rating
                    <select name="rating" required>
                        <option value="">Choose...</option>
                        <option value="5" <%= "5".equals(typedRating) ? "selected" : "" %>>5 - Excellent</option>
                        <option value="4" <%= "4".equals(typedRating) ? "selected" : "" %>>4 - Good</option>
                        <option value="3" <%= "3".equals(typedRating) ? "selected" : "" %>>3 - Average</option>
                        <option value="2" <%= "2".equals(typedRating) ? "selected" : "" %>>2 - Poor</option>
                        <option value="1" <%= "1".equals(typedRating) ? "selected" : "" %>>1 - Very poor</option>
                    </select>
                </label>
            </div>
            <label class="field">Your review
                <textarea name="text" rows="4" minlength="5" maxlength="500" required placeholder="What did you like or dislike?"><%= HtmlUtil.esc(typedText) %></textarea>
            </label>
            <button type="submit" class="btn">Submit review</button>
        </form>
    </section>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
