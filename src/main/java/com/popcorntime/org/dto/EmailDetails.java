package com.popcorntime.org.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails implements Serializable {
    private String userEmail;
    private Double totalPrice;
    private List<Long> seatIds;
}
