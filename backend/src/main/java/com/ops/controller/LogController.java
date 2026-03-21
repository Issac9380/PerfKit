package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;
import com.ops.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/log")
@RequiredArgsConstructor
public class LogController {

    private final LogAnalysisService logAnalysisService;

    @PostMapping("/analyze")
    public Result<LogAnalyzeResult> analyze(@RequestBody LogAnalyzeRequest request) {
        return Result.success(logAnalysisService.analyze(request));
    }

    @GetMapping("/containers/{clusterId}")
    public Result<?> getContainers(@PathVariable Long clusterId, @RequestParam String namespace) {
        // TODO: 返回可分析的容器列表
        return Result.success(List.of());
    }
}
