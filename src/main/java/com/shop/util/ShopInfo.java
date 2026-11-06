package com.shop.util;

/**
 * The shop name and contact details, used by every page.
 * Change the values below and the whole website updates.
 * (These are SAMPLE values: put your friend's real details here.)
 *
 * Why the values are written as String.valueOf("..."): a plain text constant is copied into the page code
 * when Tomcat compiles a page, so a later change would not show until Tomcat's work folder is cleaned.
 * With String.valueOf the pages read the newest value every time, so after you change a value a normal
 * Restart of the server is enough.
 */
public final class ShopInfo {

    public static final String NAME = String.valueOf("Subhagruha");

    /** Shown on the website. */
    public static final String PHONE_DISPLAY = String.valueOf("+91 90000 00000");
    /** Used for the call link: country code and number, no spaces. */
    public static final String PHONE_DIAL = String.valueOf("+919000000000");
    /** WhatsApp number: country code and number, digits only (no + sign). */
    public static final String WHATSAPP = String.valueOf("919000000000");
    public static final String EMAIL = String.valueOf("hello@example.com");
    public static final String ADDRESS = String.valueOf("Your shop address, City, State, PIN code");
    public static final String HOURS = String.valueOf("Monday to Saturday, 9:00 AM to 7:00 PM");

    /**
     * Demo payment screen (a pretend UPI page, no real money). It is only used when NO Razorpay keys are set.
     * Keep it true for practice and portfolio demos. Set it to false on a REAL shop, so nobody can
     * "pay" without paying.
     */
    public static final boolean ALLOW_DEMO_PAYMENT = true;

    private ShopInfo() { }
}
