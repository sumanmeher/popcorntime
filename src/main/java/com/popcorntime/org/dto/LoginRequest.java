package com.popcorntime.org.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String password;    
}
