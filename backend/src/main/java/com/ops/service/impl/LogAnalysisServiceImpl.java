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

/**
 * LogAnalysisServiceImpl 实现类
 * 实现日志分析的业务逻辑，包括从Kubernetes集群获取日志、匹配案例库、调用AI分析等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisServiceImpl implements LogAnalysisService {

    /**
     * K8sClusterMapper数据库操作类
     * 用于查询Kubernetes集群配置信息
     */
    private final K8sClusterMapper clusterMapper;

    /**
     * LogPathConfigMapper数据库操作类
     * 用于查询日志路径配置信息
     */
    private final LogPathConfigMapper logPathConfigMapper;

    /**
     * CaseLibraryMapper数据库操作类
     * 用于查询案例库信息，用于日志问题匹配
     */
    private final CaseLibraryMapper caseLibraryMapper;

    /**
     * KubernetesClientFactory工厂类
     * 用于创建Kubernetes客户端连接获取日志
     */
    private final KubernetesClientFactory clientFactory;

    /**
     * 执行日志分析
     * 整合日志获取、案例匹配、AI分析等功能，返回完整的分析结果
     *
     * @param request 日志分析请求参数，包含集群ID、命名空间、Pod名称、容器名称、容器类型等
     * @return 日志分析结果，包含日志内容、匹配案例、AI分析结果、汇总信息
     * @throws BusinessException 集群不存在时抛出404异常
     */
    @Override
    public LogAnalyzeResult analyze(LogAnalyzeRequest request) {
        // 验证集群是否存在
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        // 获取日志内容
        String logContent = getLogContent(request);

        // 根据容器类型匹配案例库
        CaseLibrary matchedCase = matchCase(logContent, request.getContainerType());

        // AI 分析（预留）
        String aiAnalysis = null;
        if (request.isUseAI()) {
            aiAnalysis = callAIAnalysis(logContent, request.getAiModel());
        }

        // 组装分析结果
        LogAnalyzeResult result = new LogAnalyzeResult();
        result.setLogContent(logContent);
        result.setMatchedCase(matchedCase);
        result.setAiAnalysis(aiAnalysis);
        result.setSummary(generateSummary(logContent, matchedCase, aiAnalysis));

        return result;
    }

    /**
     * 获取容器日志内容
     * 通过Kubernetes API获取指定容器实时日志
     *
     * @param request 日志分析请求参数
     * @return 容器日志内容字符串
     * @throws BusinessException 获取日志失败时抛出异常
     */
    private String getLogContent(LogAnalyzeRequest request) {
        // 查询集群配置
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            // 从指定命名空间、Pod、容器获取日志
            return client.pods().inNamespace(request.getNamespace())
                    .withName(request.getPodName())
                    .inContainer(request.getContainerName())
                    .getLog();
        } catch (Exception e) {
            log.error("Failed to get log", e);
            throw new BusinessException(1001, "获取日志失败: " + e.getMessage());
        }
    }

    /**
     * 匹配案例库
     * 根据日志内容和容器类型，在案例库中查找匹配的问题案例
     *
     * @param logContent 日志内容
     * @param containerType 容器类型
     * @return 匹配到的案例实体，未匹配返回null
     */
    private CaseLibrary matchCase(String logContent, String containerType) {
        // 查询指定容器类型的案例列表
        List<CaseLibrary> cases = caseLibraryMapper.selectList(
                new QueryWrapper<CaseLibrary>()
                        .eq("container_type", containerType)
        );

        // 遍历案例，查找问题模式匹配
        for (CaseLibrary c : cases) {
            if (c.getProblemPattern() != null && logContent.contains(c.getProblemPattern())) {
                return c;
            }
        }
        return null;
    }

    /**
     * 调用AI进行日志分析
     * 预留的AI分析接口，目前返回占位信息
     *
     * @param logContent 日志内容
     * @param model 使用的AI模型名称
     * @return AI分析结果
     */
    private String callAIAnalysis(String logContent, String model) {
        // TODO: 实现 AI 分析调用
        return "AI 分析功能预留";
    }

    /**
     * 生成分析结果摘要
     * 根据匹配到的案例和AI分析结果生成摘要信息
     *
     * @param logContent 日志内容
     * @param matchedCase 匹配到的案例
     * @param aiAnalysis AI分析结果
     * @return 格式化的摘要字符串
     */
    private String generateSummary(String logContent, CaseLibrary matchedCase, String aiAnalysis) {
        StringBuilder sb = new StringBuilder();
        // 如果有匹配的案例，添加案例标题和解决方案
        if (matchedCase != null) {
            sb.append("匹配案例: ").append(matchedCase.getTitle()).append("\n");
            sb.append("解决方案: ").append(matchedCase.getSolution());
        }
        return sb.toString();
    }
}
