package com.popcorntime.org.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBookingDetails implements Serializable {
    private Long seatId;
    private Double price;
    private Long showId;
}
