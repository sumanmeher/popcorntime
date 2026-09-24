package com.popcorntime.org.dto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScreenDetails {
    private String name;
    Map<LocalDate, List<ShowDetails>> shows = new LinkedHashMap<>();
}
