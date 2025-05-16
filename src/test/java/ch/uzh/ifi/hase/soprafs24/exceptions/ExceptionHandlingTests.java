package ch.uzh.ifi.hase.soprafs24.exceptions;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ExceptionHandlingTests {

    @Test
    void testCustomResponseException_withHttpStatus() {
        String message = "Test exception message";
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        CustomResponseException exception = new CustomResponseException(message, status);

        assertEquals(message, exception.getMessage());
        assertEquals(status, exception.getHttpStatus());
    }

    @Test
    void testCustomResponseException_withoutHttpStatus() {
        String message = "Another test message";
        CustomResponseException exception = new CustomResponseException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testErrorResponse() {
        int statusValue = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/resource";
        long timestampValue = System.currentTimeMillis();

        ErrorResponse errorResponse = new ErrorResponse(statusValue, error, message, path, timestampValue);

        assertEquals(statusValue, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertEquals(timestampValue, errorResponse.getTimestamp());

        errorResponse.setStatus(200);
        errorResponse.setError("OK");
        errorResponse.setMessage("Success");
        errorResponse.setPath("/api/success");
        errorResponse.setTimestamp(timestampValue + 1000);

        assertEquals(200, errorResponse.getStatus());
        assertEquals("OK", errorResponse.getError());
        assertEquals("Success", errorResponse.getMessage());
        assertEquals("/api/success", errorResponse.getPath());
        assertEquals(timestampValue + 1000, errorResponse.getTimestamp());
    }

    @Test
    void testGlobalExceptionHandler_handleCustomResponseException_withHttpStatus() {
        String message = "Custom error";
        HttpStatus status = HttpStatus.FORBIDDEN;
        CustomResponseException exception = new CustomResponseException(message, status);
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, String>> response = handler.handleCustomResponseException(exception);

        assertEquals(status, response.getStatusCode());
        assertEquals(message, response.getBody().get("message"));
    }

    @Test
    void testGlobalExceptionHandler_handleCustomResponseException_withoutHttpStatus() {
        String message = "Default custom error";
        CustomResponseException exception = new CustomResponseException(message);
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, String>> response = handler.handleCustomResponseException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(message, response.getBody().get("message"));
    }

    @Test
    void testGlobalExceptionAdvice_handleConflict_IllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Illegal argument");
        GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<Object> response = advice.handleConflict(exception, request);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), errorResponse.getError());
        assertEquals("Illegal argument", errorResponse.getMessage());
    }

    @Test
    void testGlobalExceptionAdvice_handleConflict_IllegalStateException() {
        IllegalStateException exception = new IllegalStateException("Illegal state");
        GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<Object> response = advice.handleConflict(exception, request);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), errorResponse.getError());
        assertEquals("Illegal state", errorResponse.getMessage());
    }

    @Test
    void testGlobalExceptionAdvice_handleTransactionSystemException() {
        TransactionSystemException exception = new TransactionSystemException("Transaction error");
        GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<Object> response = advice.handleTransactionSystemException(exception, request);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), errorResponse.getError());
        assertEquals("Transaction error", errorResponse.getMessage());
    }

    @Test
    void testGlobalExceptionAdvice_handleAllExceptions_ResponseStatusException() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found here");
        GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<Object> response = advice.handleAllExceptions(exception, request);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorResponse.getError());
        assertEquals("Resource not found here", errorResponse.getMessage());
    }

    @Test
    void testGlobalExceptionAdvice_handleAllExceptions_GenericException() {
        Exception exception = new Exception("General error");
        GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<Object> response = advice.handleAllExceptions(exception, request);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), errorResponse.getError());
        assertEquals("General error", errorResponse.getMessage());
    }
}