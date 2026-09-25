package com.popcorntime.org.payment;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popcorntime.org.exception.BadRequestException;
import com.popcorntime.org.payment.razorpaygateway.RazorpayPaymentRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RazorpayPaymentService implements PaymentService<PaymentRequest<RazorpayPaymentRequest>, PaymentResponse> {

    @Value("${api.razorpay.key.id}")
    private String keyId;

    @Value("${api.razorpay.key.secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    /**
     * Initializes the RazorpayClient using the configured key ID and secret.
     * Called automatically after bean construction.
     */
    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
        } catch (Exception e) {
            log.error("Failed to initialize RazorpayClient: " + e.getMessage());
        }
    }

    /**
     * Processes a payment by creating an order in Razorpay.
     * Converts the total price to paise and sends an order creation request.
     *
     * @param paymentRequest The payment request wrapper containing Razorpay specific details.
     * @param totalPrice The total price in INR.
     * @return PaymentResponse containing the created Razorpay order charge ID.
     * @throws BadRequestException If the order creation fails or returns null.
     */
    @Override
    public PaymentResponse pay(PaymentRequest<RazorpayPaymentRequest> paymentRequest, Double totalPrice) {
        try {
            RazorpayPaymentRequest razorpayPaymentRequest = getRazorpayPaymentRequest(paymentRequest);

            JSONObject orderRequest = new JSONObject();
            // Razorpay accepts amount in smallest currency unit (paise for INR)
            orderRequest.put("amount", (int) (totalPrice * 100));
            orderRequest.put("currency", "INR");
            
            if (paymentRequest.getIdempotencyKey() != null) {
                orderRequest.put("receipt", paymentRequest.getIdempotencyKey());
            } else {
                orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
            }

            Order order = razorpayClient.orders.create(orderRequest);

            if (order == null || order.get("id") == null) {
                throw new BadRequestException("Payment failed to initialize.");
            }

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setChargeId(order.get("id"));
            paymentResponse.setSellerMessage("Order created successfully in Razorpay");

            return paymentResponse;
        } catch (Exception e) {
            log.error("Razorpay Charge error: " + e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Deserializes the inner payment request object into a RazorpayPaymentRequest.
     *
     * @param paymentRequest The wrapper payment request.
     * @return The extracted RazorpayPaymentRequest object.
     * @throws IOException If JSON processing fails.
     */
    private static RazorpayPaymentRequest getRazorpayPaymentRequest(
            PaymentRequest<RazorpayPaymentRequest> paymentRequest)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        byte[] requestBytes = mapper.writeValueAsBytes(paymentRequest.getT());
        return mapper.readValue(requestBytes, RazorpayPaymentRequest.class);
    }
}
