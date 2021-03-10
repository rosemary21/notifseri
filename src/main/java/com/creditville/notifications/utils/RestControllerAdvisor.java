package com.creditville.notifications.utils;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.exceptions.CustomUncheckedException;
import com.creditville.notifications.models.response.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value ={ CustomCheckedException.class })
    protected ResponseEntity<?> handleCheckedExceptions(CustomCheckedException ex, WebRequest request){
        ex.printStackTrace();
        ErrorResponse resp = new ErrorResponse(ex.getMessage());
        return handleExceptionInternal(ex, resp, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value ={ CustomUncheckedException.class })
    protected ResponseEntity<?> handleUncheckedExceptions(CustomUncheckedException ex, WebRequest request){
        ex.printStackTrace();
        ErrorResponse resp = new ErrorResponse(ex.getMessage());
        return handleExceptionInternal(ex, resp, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value ={ Exception.class })
    protected ResponseEntity<?> handleGeneralExceptions(Exception ex, WebRequest request){
        ex.printStackTrace();
        ErrorResponse resp = new ErrorResponse("An internal server error occurred: "+ ex.getMessage());
        return handleExceptionInternal(ex, resp, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request){
        ex.printStackTrace();
        ErrorResponse resp = new ErrorResponse(ex.getBindingResult().getFieldError().getDefaultMessage());
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }
}
