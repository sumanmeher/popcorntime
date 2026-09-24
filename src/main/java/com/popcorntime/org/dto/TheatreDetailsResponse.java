package com.popcorntime.org.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheatreDetailsResponse implements Serializable {
    private Long id;
    private String name;
}
