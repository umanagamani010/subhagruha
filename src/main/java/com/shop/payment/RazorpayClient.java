package com.shop.payment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Talks to Razorpay: creates an order (server side) and verifies the payment signature. */
public final class RazorpayClient {

    private static final Pattern ORDER_ID = Pattern.compile("\"id\"\\s*:\\s*\"(order_[A-Za-z0-9]+)\"");

    private RazorpayClient() { }

    /**
     * Creates a Razorpay order. The amount is in paise (Rs. 360.00 = 36000).
     * @return the Razorpay order id, for example order_Abc123
     */
    public static String createOrder(long amountPaise, String receipt) throws IOException {
        String body = "{\"amount\":" + amountPaise + ",\"currency\":\"INR\",\"receipt\":\"" + receipt + "\"}";
        String basic = Base64.getEncoder().encodeToString(
                (RazorpayConfig.keyId() + ":" + RazorpayConfig.keySecret()).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(URI.create(RazorpayConfig.apiBase() + "/v1/orders"))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("Razorpay order failed. HTTP " + response.statusCode() + ": " + response.body());
                throw new IOException("Razorpay could not create the payment order (HTTP "
                        + response.statusCode() + "). Please check your test keys.");
            }
            Matcher m = ORDER_ID.matcher(response.body());
            if (!m.find()) {
                throw new IOException("Razorpay answered, but no order id was found.");
            }
            return m.group(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Payment order request was interrupted.");
        }
    }

    /**
     * Checks the signature sent back by Razorpay Checkout:
     * HMAC-SHA256 of  orderId + "|" + paymentId  with the key secret.
     * orderId must be the one stored on YOUR server, not the one sent by the browser.
     */
    public static boolean verifySignature(String orderId, String paymentId, String signature, String secret) {
        if (orderId == null || paymentId == null || signature == null || secret == null || secret.isEmpty()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return MessageDigest.isEqual(
                    hex.toString().getBytes(StandardCharsets.UTF_8),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
