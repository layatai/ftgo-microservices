package com.ftgo.common.exception;

public class FTGOException extends RuntimeException {
    public FTGOException(String message) {
        super(message);
    }

    public FTGOException(String message, Throwable cause) {
        super(message, cause);
    }
}

