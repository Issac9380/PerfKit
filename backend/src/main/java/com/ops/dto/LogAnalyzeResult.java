package com.ops.dto;

import com.ops.entity.CaseLibrary;
import lombok.Data;

@Data
public class LogAnalyzeResult {
    private String logContent;
    private CaseLibrary matchedCase;
    private String aiAnalysis;
    private String summary;
}
