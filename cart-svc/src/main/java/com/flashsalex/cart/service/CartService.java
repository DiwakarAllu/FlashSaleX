package com.flashsalex.cart.service;

import com.flashsalex.cart.client.ProductServiceClient;
import com.flashsalex.cart.dto.AddCartItemRequest;
import com.flashsalex.cart.dto.CartItemResponse;
import com.flashsalex.cart.dto.CartResponse;
import com.flashsalex.cart.dto.UpdateCartItemRequest;
import com.flashsalex.cart.entity.Cart;
import com.flashsalex.cart.entity.CartItem;
import com.flashsalex.cart.exception.ResourceNotFoundException;
import com.flashsalex.cart.repository.CartRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        productServiceClient.getProduct(request.productId());

        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> Cart.builder().userId(userId).build());

        CartItem existingItem = cart.getItems().stream()
            .filter(item -> item.getProductId().equals(request.productId()))
            .findFirst()
            .orElse(null);

        if (existingItem != null) {
            try {
                existingItem.setQuantity(Math.addExact(
                    existingItem.getQuantity(),
                    request.quantity()
                ));
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("Quantity is too large");
            }
        } else {
            CartItem item = CartItem.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .build();
            cart.addItem(item);
        }

        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return buildCartResponse(getUserCart(userId));
    }

    @Transactional
    public CartResponse updateItem(
        Long userId,
        String productId,
        UpdateCartItemRequest request
    ) {
        Cart cart = getUserCart(userId);
        CartItem item = cart.getItems().stream()
            .filter(current -> current.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

        productServiceClient.getProduct(productId);
        item.setQuantity(request.quantity());
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, String productId) {
        Cart cart = getUserCart(userId);
        CartItem item = cart.getItems().stream()
            .filter(current -> current.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

        cart.removeItem(item);
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getUserCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getUserCart(Long userId) {
        return cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
            .map(item -> {
                var product = productServiceClient.getProduct(item.getProductId());
                BigDecimal subtotal = product.price()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                return new CartItemResponse(
                    item.getProductId(),
                    product.name(),
                    item.getQuantity(),
                    product.price(),
                    subtotal,
                    product.currency()
                );
            })
            .toList();

        BigDecimal total = itemResponses.stream()
            .map(CartItemResponse::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency = itemResponses.isEmpty()
            ? null
            : itemResponses.getFirst().currency();

        return new CartResponse(
            cart.getCartId(),
            cart.getUserId(),
            itemResponses,
            total,
            currency,
            cart.getCreatedAt(),
            cart.getUpdatedAt()
        );
    }
}
