package com.ops.service;

import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;

public interface LogAnalysisService {
    LogAnalyzeResult analyze(LogAnalyzeRequest request);
}
