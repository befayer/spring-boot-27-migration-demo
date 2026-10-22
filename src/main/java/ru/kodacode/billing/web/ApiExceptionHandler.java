package ru.kodacode.billing.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kodacode.billing.invoice.InvoiceAlreadyPaidException;
import ru.kodacode.billing.invoice.InvoiceNotFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvoiceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(InvoiceNotFoundException exception) {
        return new ApiError("INVOICE_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(InvoiceAlreadyPaidException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError alreadyPaid(InvoiceAlreadyPaidException exception) {
        return new ApiError("INVOICE_ALREADY_PAID", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining("; "));
        return new ApiError("VALIDATION_FAILED", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError unreadable(HttpMessageNotReadableException exception) {
        return new ApiError("MALFORMED_REQUEST", "Request body is not valid JSON");
    }
}
