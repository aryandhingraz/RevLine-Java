package com.revline.model;

import com.revline.util.Validator;
import com.revline.util.exception.InvalidCarException;

import java.time.LocalDateTime;

/**
 * Immutable message carrier representing a buyer purchase request in the concurrent sales pipeline (L15, L16).
 */
public class PurchaseRequest {

    private final int requestId;
    private final int carId;
    private final String buyerName;
    private final String sellerName;
    private final LocalDateTime requestTimestamp;

    public PurchaseRequest(int requestId, int carId, String buyerName, String sellerName) {
        if (requestId <= 0) {
            throw new InvalidCarException("Request ID must be positive.");
        }
        if (carId <= 0) {
            throw new InvalidCarException("Car ID must be positive.");
        }
        Validator.requireNonBlank(buyerName, "Buyer Name");
        Validator.requireNonBlank(sellerName, "Seller Name");

        this.requestId = requestId;
        this.carId = carId;
        this.buyerName = buyerName.trim();
        this.sellerName = sellerName.trim();
        this.requestTimestamp = LocalDateTime.now();
    }

    public int getRequestId() {
        return requestId;
    }

    public int getCarId() {
        return carId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    @Override
    public String toString() {
        return String.format("PurchaseRequest #%d [Car #%d for '%s' by '%s' @ %s]",
                requestId, carId, buyerName, sellerName, requestTimestamp);
    }
}
