package com.popcorntime.org.payment;

/**
 * 
 * "Any class that implements this interface must have a pay method. This method
 * will accept some Request object of type T, a total price, and it must return
 * a Response object of type R."
 */
public interface PaymentService<T, R> {
    /**
     * Executes the payment processing logic for a specific payment provider.
     *
     * @param request The specific payment provider request payload.
     * @param totalPrice The amount to be paid.
     * @return R The response from the payment provider indicating success or failure.
     */
    R pay(T request, Double totalPrice);
}
