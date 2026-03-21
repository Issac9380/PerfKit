package com.ops.dto;

import lombok.Data;

@Data
public class ExecuteCommandResult {
    private String command;
    private boolean success;
    private String output;
    private String error;
}
