package com.ops.service;

import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;

public interface PerformanceService {
    ExecuteCommandResult execute(ExecuteCommandRequest request);
}
