package com.popcorntime.org.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.popcorntime.org.exception.BadRequestException;

/**
 * Interface defining the contract for retrieving a specific payment provider.
 * Allows for dynamic resolution of payment gateways.
 *
 * @param <T> The type of payment service to return.
 */
public interface PaymentProviderService<T> {
    /**
     * Retrieves the specific payment provider implementation based on the requested type.
     *
     * @param paymentProviderType The type of payment provider (e.g., RAZORPAY, GOOGLEPAY).
     * @return T The payment service implementation corresponding to the given type.
     */
    T getProvider(PaymentProviderType paymentProviderType);
}

/**
 * Implementation of {@link PaymentProviderService} that resolves the appropriate
 * payment service based on the requested {@link PaymentProviderType}.
 * Acts as a factory to fetch the correct payment gateway component from the Spring context.
 */
@Component
class PaymentProviderServiceImpl
        implements PaymentProviderService<PaymentService<PaymentRequest, PaymentResponse>> {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Used to get Payment Provider Service based on request
     *
     * @param paymentProviderType The type of payment provider to instantiate.
     * @return The specific {@link PaymentService} implementation.
     */
    @Override
    @SuppressWarnings(value = "rawtypes")
    public PaymentService getProvider(PaymentProviderType paymentProviderType) {
        switch (paymentProviderType) {
            case RAZORPAY -> {
                return applicationContext.getBean(RazorpayPaymentService.class);
            }
            case GOOGLEPAY ->
                throw new BadRequestException("GOOGLEPAY is Yet to be configured for receiving Payments.");
            case PHONEPE -> throw new BadRequestException("PHONEPE is Yet to be configured for receiving Payments.");
            case PAYTM -> throw new BadRequestException("PAYTM is Yet to be configured for receiving Payments.");
            default -> throw new BadRequestException("InCompatible Payment Provider requested.");
        }
    }
}
