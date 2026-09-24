package com.popcorntime.org.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDetailsDto {

    private String movieName;
    private Map<Long, ScreenDetails> screens = new HashMap<>();

}
