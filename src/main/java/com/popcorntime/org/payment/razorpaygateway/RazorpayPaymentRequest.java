package com.popcorntime.org.payment.razorpaygateway;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayPaymentRequest {
    private String username;
    private boolean success;
    private Map<String, Object> additionalInfo;
    private String orderId; // equivalent to chargeId, represented as a String in Razorpay
}
