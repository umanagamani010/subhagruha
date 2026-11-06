package com.shop.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** The shopping cart. One Cart object is stored in each user's HttpSession. */
public class Cart {
    private static final int MAX_PER_ITEM = 20;

    // key = product id, value = cart line (LinkedHashMap keeps the order added)
    private final Map<Integer, CartItem> items = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        CartItem existing = items.get(product.getId());
        if (existing == null) {
            items.put(product.getId(), new CartItem(product, Math.min(quantity, MAX_PER_ITEM)));
        } else {
            existing.setQuantity(Math.min(existing.getQuantity() + quantity, MAX_PER_ITEM));
        }
    }

    public void update(int productId, int quantity) {
        if (quantity <= 0) {
            items.remove(productId);
        } else {
            CartItem item = items.get(productId);
            if (item != null) {
                item.setQuantity(Math.min(quantity, MAX_PER_ITEM));
            }
        }
    }

    public void remove(int productId) { items.remove(productId); }

    public void clear() { items.clear(); }

    public boolean isEmpty() { return items.isEmpty(); }

    public Collection<CartItem> getItems() { return new ArrayList<>(items.values()); }

    /** Total number of pieces (used for the "View Cart (3)" label). */
    public int getCount() {
        int count = 0;
        for (CartItem item : items.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items.values()) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }
}
