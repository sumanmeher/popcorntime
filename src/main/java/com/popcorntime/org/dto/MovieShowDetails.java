package com.popcorntime.org.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MovieShowDetails {

    Long getScreenId();

    String getScreenName();

    Long getMovieId();

    String getMovieName();

    LocalDate getDate();

    LocalTime getShowTime();

    Long getShowId();

    Double getSeatPrice();
}
