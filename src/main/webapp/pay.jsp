<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.shop.model.Order, com.shop.util.HtmlUtil" %>
<%!
    // keeps only safe characters, because these values are written inside a JavaScript string
    private static String jsSafe(String s) {
        return s == null ? "" : s.replaceAll("[^A-Za-z0-9 @.+_-]", "");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pay for your order - <%= com.shop.util.ShopInfo.NAME %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<%
    Order order = (Order) request.getAttribute("order");
    boolean notConfigured = request.getAttribute("notConfigured") != null;
    boolean demoMode = request.getAttribute("demoMode") != null;
    String payError = (String) request.getAttribute("payError");
    String keyId = (String) request.getAttribute("keyId");
    String rzpOrderId = (String) request.getAttribute("rzpOrderId");
    Long amountPaise = (Long) request.getAttribute("amountPaise");
    boolean ready = !notConfigured && !demoMode && payError == null && keyId != null && rzpOrderId != null && amountPaise != null;
%>

<main class="container">
    <h1 class="page-title">Pay for order #<%= order.getId() %></h1>

    <% if (request.getParameter("failed") != null) { %>
        <div class="error">We could not confirm your payment. If money was deducted, please contact the shop with order number #<%= order.getId() %>.</div>
    <% } %>

    <div class="panel" style="max-width:560px">
        <h3>Amount to pay</h3>
        <p class="price">&#8377;<%= order.getTotal() %></p>

        <% if (demoMode) { %>
            <div class="demo-banner">DEMO PAYMENT: this is a pretend UPI screen. No real money is used.</div>
            <% if (request.getParameter("demoFailed") != null) { %>
                <div class="error">Demo payment failed. Your order is still unpaid: you can try again or cancel the order.</div>
            <% } %>
            <form action="<%= ctx %>/payment/demo" method="post" class="demo-pay">
                <label class="field">UPI ID <span class="hint">(any text, nothing is checked)</span>
                    <input type="text" name="upi" placeholder="yourname@upi" maxlength="60" autocomplete="off">
                </label>
                <button type="submit" name="result" value="success" class="btn block">Pay &#8377;<%= order.getTotal() %> (demo)</button>
                <button type="submit" name="result" value="fail" class="btn ghost block" style="margin-top:10px">Simulate a failed payment</button>
            </form>
        <% } else if (notConfigured) { %>
            <div class="error">Online payment is not set up yet. The shop owner must add the Razorpay keys (see README).</div>
        <% } else if (payError != null) { %>
            <div class="error"><%= HtmlUtil.esc(payError) %></div>
            <p><a class="btn" href="<%= ctx %>/pay">Try again</a></p>
        <% } else if (ready) { %>
            <p>You will pay on the secure Razorpay window (UPI, card, netbanking or wallet).</p>
            <button type="button" id="payBtn" class="btn block">Pay &#8377;<%= order.getTotal() %> now</button>
            <p id="message" class="note" style="margin-top:12px;color:#991b1b"></p>
        <% } %>

        <form action="<%= ctx %>/payment/cancel" method="post" style="margin-top:16px;text-align:center">
            <button type="submit" class="link">Cancel this order</button>
        </form>
    </div>

    <form id="verifyForm" action="<%= ctx %>/payment/verify" method="post">
        <input type="hidden" name="razorpay_payment_id" id="rzp_payment_id">
        <input type="hidden" name="razorpay_order_id" id="rzp_order_id">
        <input type="hidden" name="razorpay_signature" id="rzp_signature">
    </form>
</main>

<%@ include file="footer.jsp" %>

<% if (ready) { %>
<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
<script>
    var options = {
        key: "<%= jsSafe(keyId) %>",
        amount: "<%= amountPaise %>",
        currency: "INR",
        name: "<%= jsSafe(com.shop.util.ShopInfo.NAME) %>",
        description: "Order #<%= order.getId() %>",
        order_id: "<%= jsSafe(rzpOrderId) %>",
        prefill: {
            name: "<%= jsSafe(order.getCustomerName()) %>",
            email: "<%= jsSafe(order.getEmail()) %>",
            contact: "<%= jsSafe(order.getPhone()) %>"
        },
        theme: { color: "#2f6b3a" },
        handler: function (response) {
            // payment done: send the proof to OUR server, which checks the signature
            document.getElementById("rzp_payment_id").value = response.razorpay_payment_id;
            document.getElementById("rzp_order_id").value = response.razorpay_order_id;
            document.getElementById("rzp_signature").value = response.razorpay_signature;
            document.getElementById("verifyForm").submit();
        },
        modal: {
            ondismiss: function () {
                document.getElementById("message").textContent =
                    "Payment not completed. You can click the button to try again, or cancel the order.";
            }
        }
    };

    document.getElementById("payBtn").onclick = function () {
        if (typeof Razorpay === "undefined") {
            document.getElementById("message").textContent =
                "The payment window could not load. Check your internet connection and reload the page.";
            return;
        }
        var rzp = new Razorpay(options);
        rzp.on("payment.failed", function (r) {
            document.getElementById("message").textContent =
                "Payment failed: " + (r.error && r.error.description ? r.error.description : "please try again.");
        });
        rzp.open();
    };
</script>
<% } %>
</body>
</html>
