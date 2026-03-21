package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.common.BusinessException;
import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;
import com.ops.entity.CaseLibrary;
import com.ops.entity.K8sCluster;
import com.ops.entity.LogPathConfig;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.CaseLibraryMapper;
import com.ops.mapper.K8sClusterMapper;
import com.ops.mapper.LogPathConfigMapper;
import com.ops.service.LogAnalysisService;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisServiceImpl implements LogAnalysisService {

    private final K8sClusterMapper clusterMapper;
    private final LogPathConfigMapper logPathConfigMapper;
    private final CaseLibraryMapper caseLibraryMapper;
    private final KubernetesClientFactory clientFactory;

    @Override
    public LogAnalyzeResult analyze(LogAnalyzeRequest request) {
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        // 获取日志内容
        String logContent = getLogContent(request);

        // 匹配案例库
        CaseLibrary matchedCase = matchCase(logContent, request.getContainerType());

        // AI 分析（预留）
        String aiAnalysis = null;
        if (request.isUseAI()) {
            aiAnalysis = callAIAnalysis(logContent, request.getAiModel());
        }

        LogAnalyzeResult result = new LogAnalyzeResult();
        result.setLogContent(logContent);
        result.setMatchedCase(matchedCase);
        result.setAiAnalysis(aiAnalysis);
        result.setSummary(generateSummary(logContent, matchedCase, aiAnalysis));

        return result;
    }

    private String getLogContent(LogAnalyzeRequest request) {
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            return client.pods().inNamespace(request.getNamespace())
                    .withName(request.getPodName())
                    .inContainer(request.getContainerName())
                    .getLog();
        } catch (Exception e) {
            log.error("Failed to get log", e);
            throw new BusinessException(1001, "获取日志失败: " + e.getMessage());
        }
    }

    private CaseLibrary matchCase(String logContent, String containerType) {
        List<CaseLibrary> cases = caseLibraryMapper.selectList(
                new QueryWrapper<CaseLibrary>()
                        .eq("container_type", containerType)
        );

        for (CaseLibrary c : cases) {
            if (c.getProblemPattern() != null && logContent.contains(c.getProblemPattern())) {
                return c;
            }
        }
        return null;
    }

    private String callAIAnalysis(String logContent, String model) {
        // TODO: 实现 AI 分析调用
        return "AI 分析功能预留";
    }

    private String generateSummary(String logContent, CaseLibrary matchedCase, String aiAnalysis) {
        StringBuilder sb = new StringBuilder();
        if (matchedCase != null) {
            sb.append("匹配案例: ").append(matchedCase.getTitle()).append("\n");
            sb.append("解决方案: ").append(matchedCase.getSolution());
        }
        return sb.toString();
    }
}
