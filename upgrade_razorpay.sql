-- You do NOT need to run this any more: the website adds the payment columns and the reviews table
-- by itself when it starts. It is kept only for people who want to run the change by hand.
USE shopdb;

ALTER TABLE orders
    ADD COLUMN razorpay_order_id   VARCHAR(40),
    ADD COLUMN razorpay_payment_id VARCHAR(40);
