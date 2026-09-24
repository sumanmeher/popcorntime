package com.popcorntime.org.dto;

import java.sql.Date;
import java.sql.Time;

public interface MovieShowTheatreDetails {
    Long getShowId();
    java.time.LocalDate getDate();
    java.time.LocalTime getShowTime();
    Double getSeatPrice();

    Long getScreenId();
    String getScreenName();

    Long getTheatreId();
    String getTheatreName();
    String getTheatreAddress();
}
