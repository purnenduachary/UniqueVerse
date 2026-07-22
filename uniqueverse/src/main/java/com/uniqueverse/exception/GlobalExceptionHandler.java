package com.uniqueverse.exception;


import com.uniqueverse.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {

//        ApiResponse<Object> response = ApiResponse.builder()
//                .success(false)
//                .message(ex.getMessage())
//                .data(null)
//                .build();
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


}