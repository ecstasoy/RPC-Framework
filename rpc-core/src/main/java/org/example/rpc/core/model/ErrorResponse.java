package org.example.rpc.core.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String errorCode;
    private String message;
}
