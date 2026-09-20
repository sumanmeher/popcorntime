package com.popcorntime.org.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class NotFoundException extends RuntimeException {

    private final String message;
    private final HttpStatus status;

    public NotFoundException(String message) {
        super(message);
        this.message = message;
        this.status = HttpStatus.NOT_FOUND;
    }
}
