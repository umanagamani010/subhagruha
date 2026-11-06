package com.shop.util;

/** Escapes text before printing it in a page, so users cannot inject HTML or scripts (XSS). */
public final class HtmlUtil {
    private HtmlUtil() { }

    public static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** Five stars for a rating, for example 4 gives four filled stars and one empty star. */
    public static String stars(double rating) {
        int full = (int) Math.round(rating);
        full = Math.max(0, Math.min(5, full));
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= full ? "\u2605" : "\u2606");
        }
        return sb.toString();
    }
}
