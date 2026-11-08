package com.example.ticktracker.web;

import com.example.ticktracker.client.UpstreamApiException;
import com.example.ticktracker.ingest.IngestionInProgressException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice

public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{
    private static final Logger log =
    LoggerFactory.getLogger(GlobalExceptionHandler.class);

@Override
protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request){
        Map<String,String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(fe -> fieldErrors.putIfAbsent(fe.getField(),fe.getDefaultMessage()));
            ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid.","validation-error");
            pd.setProperty("errors",fieldErrors);
            return ResponseEntity.badRequest().body(pd);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex){
        Map<String,String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations()
            .forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more parameters are invalid.", "validation-error");
        pd.setProperty("errors", errors);
        return pd;
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex){
        String expected = ex.getRequiredType() != null
        ? ex.getRequiredType().getSimpleName() : "Valid value";
        return problem(HttpStatus.BAD_REQUEST, "Invalid parameter", "Parameter '%s' has invalid value '%s' (expected %s).".formatted(ex.getName(), ex.getValue(), expected),"invalid-parameter");
    }
    @ExceptionHandler(DateTimeParseException.class)
    public ProblemDetail handleDateTimeParseException(DateTimeParseException ex){
        return problem(HttpStatus.BAD_REQUEST, "Invalid date", "could not parse date '%s'. Use ISO format year month day.".formatted(ex.getParsedString()),"invalid-date");
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex){
        return problem(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), "bad-request");
    }
    @ExceptionHandler(IngestionInProgressException.class)
    public ProblemDetail handleIngestionInProgress(IngestionInProgressException ex){
        return problem(HttpStatus.CONFLICT, "Ingestion in progress", ex.getMessage(), "ingestion-in-progress");
    }
    @ExceptionHandler(UpstreamApiException.class)
    public ProblemDetail handleUpstreamApi(UpstreamApiException ex){
        log.error("Upstream service failure: {}", ex.getMessage());
        return problem(HttpStatus.BAD_GATEWAY, "Upstream service error", ex.getMessage(), "upstream-error");
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ProblemDetail handleUpstreamResponse(RestClientResponseException ex){
        log.error("Upstream service error: {} {}", ex.getStatusCode(),ex.getMessage());
        return problem(HttpStatus.BAD_GATEWAY, "Upstream service error","The tick sightings data source returned an error (%d). please try again later.".formatted(ex.getStatusCode().value()),"upstream-error");
    }
    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleUpstreamUnavailable(ResourceAccessException ex){
        log.error("Upstream API unreachable", ex);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Data source unavailable", "The tick sighting data source could not be reached. please try again later.", "upstream-unavailable");

    }
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex){
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occured.", "internal-error");

    }
    private ProblemDetail problem(HttpStatus status, String title, String detail, String type){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://tick-tracker.local/problems/" + type));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
