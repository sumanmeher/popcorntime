package com.popcorntime.org.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsDto implements Serializable {
    private String userEmail;
    private Long showId;
    private List<Long> seatIds = new ArrayList<>();
    private Double totalPrice;
    private String chargeId;
}
