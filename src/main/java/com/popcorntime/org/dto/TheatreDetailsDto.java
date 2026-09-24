package com.popcorntime.org.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheatreDetailsDto {

    private String name;
    private String address;
    private Map<Long, ScreenDetails> screens = new HashMap<>();

}
