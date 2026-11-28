package com.flashsalex.inventory.service;

import com.flashsalex.inventory.dto.ReservationItemRequest;
import com.flashsalex.inventory.dto.ReservationItemResponse;
import com.flashsalex.inventory.dto.ReservationRequest;
import com.flashsalex.inventory.dto.ReservationResponse;
import com.flashsalex.inventory.dto.StockAdjustmentRequest;
import com.flashsalex.inventory.dto.StockResponse;
import com.flashsalex.inventory.entity.InventoryReservation;
import com.flashsalex.inventory.entity.InventoryReservationItem;
import com.flashsalex.inventory.entity.InventoryStock;
import com.flashsalex.inventory.entity.ReservationStatus;
import com.flashsalex.inventory.exception.InsufficientStockException;
import com.flashsalex.inventory.exception.ReservationStateException;
import com.flashsalex.inventory.exception.ResourceNotFoundException;
import com.flashsalex.inventory.repository.InventoryReservationRepository;
import com.flashsalex.inventory.repository.InventoryStockRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryStockRepository stockRepository;
    private final InventoryReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public StockResponse getStock(String productId) {
        InventoryStock stock = stockRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventory not found for product: " + productId
            ));
        return toStockResponse(stock);
    }

    @Transactional
    public StockResponse addStock(String productId, StockAdjustmentRequest request) {
        InventoryStock stock = stockRepository.findByProductIdForUpdate(productId)
            .orElseGet(() -> new InventoryStock(productId));
        try {
            stock.addStock(request.quantity());
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Stock quantity is too large");
        }
        return toStockResponse(stockRepository.save(stock));
    }

    @Transactional
    public ReservationResponse reserve(Long userId, ReservationRequest request) {
        TreeMap<String, Integer> quantities = aggregateItems(request.items());

        for (var item : quantities.entrySet()) {
            InventoryStock stock = stockRepository.findByProductIdForUpdate(item.getKey())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Inventory not found for product: " + item.getKey()
                ));
            try {
                stock.reserve(item.getValue());
            } catch (IllegalStateException ex) {
                throw new InsufficientStockException(item.getKey());
            }
        }

        InventoryReservation reservation = new InventoryReservation(userId);
        quantities.forEach(reservation::addItem);
        return toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse confirm(Long userId, UUID reservationId) {
        InventoryReservation reservation = findReservationForUpdate(userId, reservationId);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return toReservationResponse(reservation);
        }
        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            throw new ReservationStateException("Released reservation cannot be confirmed");
        }

        for (InventoryReservationItem item : sortedItems(reservation)) {
            InventoryStock stock = findStockForUpdate(item.getProductId());
            stock.confirm(item.getQuantity());
        }
        reservation.confirm();
        return toReservationResponse(reservation);
    }

    @Transactional
    public ReservationResponse release(Long userId, UUID reservationId) {
        InventoryReservation reservation = findReservationForUpdate(userId, reservationId);
        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return toReservationResponse(reservation);
        }
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            throw new ReservationStateException("Confirmed reservation cannot be released");
        }

        for (InventoryReservationItem item : sortedItems(reservation)) {
            InventoryStock stock = findStockForUpdate(item.getProductId());
            stock.release(item.getQuantity());
        }
        reservation.release();
        return toReservationResponse(reservation);
    }

    private TreeMap<String, Integer> aggregateItems(List<ReservationItemRequest> items) {
        TreeMap<String, Integer> quantities = new TreeMap<>();
        Set<String> productIds = new HashSet<>();
        for (ReservationItemRequest item : items) {
            String productId = item.productId().trim();
            if (!productIds.add(productId)) {
                throw new IllegalArgumentException("Duplicate product ID: " + productId);
            }
            quantities.put(productId, item.quantity());
        }
        return quantities;
    }

    private InventoryReservation findReservationForUpdate(Long userId, UUID reservationId) {
        return reservationRepository.findByIdAndUserIdForUpdate(reservationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    private InventoryStock findStockForUpdate(String productId) {
        return stockRepository.findByProductIdForUpdate(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventory not found for product: " + productId
            ));
    }

    private List<InventoryReservationItem> sortedItems(InventoryReservation reservation) {
        return reservation.getItems().stream()
            .sorted((first, second) -> first.getProductId().compareTo(second.getProductId()))
            .toList();
    }

    private StockResponse toStockResponse(InventoryStock stock) {
        return new StockResponse(
            stock.getProductId(),
            stock.getQuantityOnHand(),
            stock.getReservedQuantity(),
            stock.getAvailableQuantity()
        );
    }

    private ReservationResponse toReservationResponse(InventoryReservation reservation) {
        List<ReservationItemResponse> items = reservation.getItems().stream()
            .map(item -> new ReservationItemResponse(item.getProductId(), item.getQuantity()))
            .toList();
        return new ReservationResponse(
            reservation.getReservationId(),
            reservation.getStatus(),
            items,
            reservation.getCreatedAt()
        );
    }
}
