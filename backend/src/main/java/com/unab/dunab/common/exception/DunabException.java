package com.unab.dunab.common.exception;

import org.springframework.http.HttpStatus;

public class DunabException extends RuntimeException {

    private final HttpStatus status;

    public DunabException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static DunabException notFound(String msg)      { return new DunabException(msg, HttpStatus.NOT_FOUND); }
    public static DunabException badRequest(String msg)    { return new DunabException(msg, HttpStatus.BAD_REQUEST); }
    public static DunabException forbidden(String msg)     { return new DunabException(msg, HttpStatus.FORBIDDEN); }
    public static DunabException conflict(String msg)      { return new DunabException(msg, HttpStatus.CONFLICT); }
}
