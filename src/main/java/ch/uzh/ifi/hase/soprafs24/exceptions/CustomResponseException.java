package ch.uzh.ifi.hase.soprafs24.exceptions;

import org.springframework.http.HttpStatus;

public class CustomResponseException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    // Constructor with both message and HttpStatus
    public CustomResponseException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = (httpStatus != null) ? httpStatus : HttpStatus.BAD_REQUEST; // Ensure status is never null
    }

    // Constructor with only message (sets a default HttpStatus)
    public CustomResponseException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST; // Default status to avoid 500
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
