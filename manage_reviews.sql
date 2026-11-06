-- Handy commands for the shop owner (run them in MySQL Workbench).
USE shopdb;

-- See all reviews
SELECT r.id, p.name AS product, r.customer_name, r.rating, r.review_text, r.visible, r.created_at
FROM reviews r JOIN products p ON p.id = r.product_id ORDER BY r.created_at DESC;

-- Hide a review (for example spam or abuse). Use the id from the list above.
-- UPDATE reviews SET visible = 0 WHERE id = 1;

-- Show it again
-- UPDATE reviews SET visible = 1 WHERE id = 1;
