package com.popcorntime.org.dto;

import lombok.Data;

@Data
public class AddTheatreRequest {
    private String name;
    private String address;
    private Integer capacity;
}
