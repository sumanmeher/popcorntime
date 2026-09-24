package com.popcorntime.org.dto;

import java.io.Serializable;

public interface BookingResponse extends Serializable {
    Long getSeatId();
    Double getSeatPrice();
}
