package ch.uzh.ifi.hase.soprafs24.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomResponseException.class)
    public ResponseEntity<Map<String, String>> handleCustomResponseException(CustomResponseException ex) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", ex.getMessage()); // Send only the message to the frontend

        HttpStatus status = (ex.getHttpStatus() != null) ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST; // Ensure status is valid

        return ResponseEntity
                .status(status.value()) // Always use a valid status
                .body(responseBody);
    }
}
