package com.popcorntime.org.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UnauthorizedException extends RuntimeException {

    private final String message;
    private final String userName;
    private final HttpStatus status;

    public UnauthorizedException(String message) {
        super(message);
        this.message = message;
        this.status = HttpStatus.UNAUTHORIZED;
        this.userName = null;
    }
}
