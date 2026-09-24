package com.popcorntime.org.dto;

import java.sql.Date;
import java.sql.Time;

public interface ShowTimeDetailsResponse {
    java.time.LocalDate getDate();
    java.time.LocalTime getShowTime();
}
