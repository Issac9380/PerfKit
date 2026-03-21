package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;
import com.ops.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final PerformanceService performanceService;

    @PostMapping("/execute")
    public Result<ExecuteCommandResult> execute(@RequestBody ExecuteCommandRequest request) {
        return Result.success(performanceService.execute(request));
    }
}
