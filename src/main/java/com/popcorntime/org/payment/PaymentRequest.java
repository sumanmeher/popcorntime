package com.popcorntime.org.payment;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest<T> {
    Set<Long> seatIds = new HashSet<>();
    private PaymentProviderType paymentProviderType;
    private Integer checkoutTotalPrice;
    private T t;
    private String idempotencyKey;
}
