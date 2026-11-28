package com.flashsalex.cart.repository;

import com.flashsalex.cart.entity.CartItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartCartIdAndProductId(UUID cartId, String productId);
}
