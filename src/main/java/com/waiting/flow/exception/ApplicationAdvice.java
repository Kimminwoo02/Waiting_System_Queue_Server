package com.waiting.flow.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApplicationAdvice {

    @ExceptionHandler(ApplicationException.class)
    Mono<?> applicationExceptionHandler(ApplicationException ex){

        return Mono.just(ResponseEntity
                .status(ex.getHttpStatus())
                .body(new serverExceptionResponse(ex.getCode(), ex.getReason())));
    }

    public record serverExceptionResponse(String code, String reason){

    }
}
