package org.example.myapp.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        // Handle validation errors
        if (exception instanceof ConstraintViolationException cve) {
            return handleValidationException(cve);
        }

        // Handle "not found" errors
        if (exception instanceof NotFoundException nfe) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                            "error", "Not Found",
                            "message", nfe.getMessage()
                    ))
                    .build();
        }

        // Handle illegal arguments
        if (exception instanceof IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "Bad Request",
                            "message", iae.getMessage()
                    ))
                    .build();
        }

        // Fallback: unexpected server errors
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                        "error", "Internal Server Error",
                        "message", exception.getMessage()
                ))
                .build();
    }

    private Response handleValidationException(ConstraintViolationException cve) {
        Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();

        for (ConstraintViolation<?> v : violations) {
            String field = v.getPropertyPath().toString();
            String message = v.getMessage();
            errors.put(field, message);
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "error", "Validation Failed",
                        "details", errors
                ))
                .build();
    }
}
