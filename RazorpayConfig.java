package com.shop.payment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the Razorpay keys. They are NEVER written in the code or kept inside the project folder.
 * Order of lookup:
 *   1. environment variables RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET
 *   2. the file "razorpay.properties" in your user folder (for example C:\Users\sai\razorpay.properties)
 */
public final class RazorpayConfig {

    private static final Properties FILE = new Properties();

    static {
        File f = new File(System.getProperty("user.home"), "razorpay.properties");
        if (f.isFile()) {
            try (InputStream in = new FileInputStream(f)) {
                FILE.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private RazorpayConfig() { }

    private static String get(String fileKey, String envKey) {
        String v = System.getenv(envKey);
        if (v == null || v.trim().isEmpty()) {
            v = FILE.getProperty(fileKey);
        }
        return v == null ? "" : v.trim();
    }

    public static String keyId() { return get("key_id", "RAZORPAY_KEY_ID"); }

    public static String keySecret() { return get("key_secret", "RAZORPAY_KEY_SECRET"); }

    /** Normally https://api.razorpay.com (only changed for local testing). */
    public static String apiBase() {
        String v = get("api_base", "RAZORPAY_API_BASE");
        return v.isEmpty() ? "https://api.razorpay.com" : v;
    }

    /** True when real-looking test or live keys are present. */
    public static boolean isConfigured() {
        String id = keyId();
        return (id.startsWith("rzp_test_") || id.startsWith("rzp_live_")) && !keySecret().isEmpty();
    }
}
