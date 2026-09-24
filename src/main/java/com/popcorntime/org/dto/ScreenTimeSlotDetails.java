package com.popcorntime.org.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ScreenTimeSlotDetails {
    Long getTimeSlotId();

    LocalDate getDate();

    LocalTime getSlotTime();
}
