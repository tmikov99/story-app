package com.coursework.story.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildErrorResponse("Unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR);
    }

    @ExceptionHandler(StoryValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(StoryValidationException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCodes.VALIDATION_FAILED,
                "Validation failed",
                ex.getErrors(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientAuthentication(InsufficientAuthenticationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, ErrorCodes.USERNAME_NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, String code) {
        ErrorResponse response = new ErrorResponse(status.value(), code, message, LocalDateTime.now());
        return new ResponseEntity<>(response, status);
    }

    public static class SimpleErrorResponse {
        private int status;
        private String code;

        private String message;

        public SimpleErrorResponse(int status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class ErrorResponse extends SimpleErrorResponse {
        private LocalDateTime timestamp;

        public ErrorResponse(int status, String code, String message, LocalDateTime timestamp) {
            super(status, code, message);
            this.timestamp = timestamp;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class ValidationErrorResponse extends ErrorResponse {
        private List<String> errors;

        public ValidationErrorResponse(int status, String code, String message, List<String> errors, LocalDateTime timestamp) {
            super(status, code, message, timestamp);
            this.errors = errors;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}