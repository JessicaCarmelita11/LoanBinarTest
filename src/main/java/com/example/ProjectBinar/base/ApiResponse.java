package com.example.ProjectBinar.base;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private String message;
    private Integer code;
    private T data;
    private Boolean success;
    private Instant timestamp;
}
