package com.ops.service.impl;

import com.ops.common.BusinessException;
import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;
import com.ops.entity.CommandTemplate;
import com.ops.entity.K8sCluster;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.CommandTemplateMapper;
import com.ops.mapper.K8sClusterMapper;
import com.ops.security.CommandValidator;
import com.ops.service.PerformanceService;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * PerformanceServiceImpl 实现类
 * 实现性能测试相关命令执行的业务逻辑，包括命令模板管理、安全校验、Kubernetes容器内命令执行等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    /**
     * K8sClusterMapper数据库操作类
     * 用于查询Kubernetes集群配置信息
     */
    private final K8sClusterMapper clusterMapper;

    /**
     * CommandTemplateMapper数据库操作类
     * 用于查询性能测试命令模板信息
     */
    private final CommandTemplateMapper templateMapper;

    /**
     * KubernetesClientFactory工厂类
     * 用于创建Kubernetes客户端连接执行命令
     */
    private final KubernetesClientFactory clientFactory;

    /**
     * CommandValidator命令校验器
     * 用于校验命令模板和参数的安全性，防止命令注入
     */
    private final CommandValidator commandValidator;

    /**
     * 执行性能测试命令
     * 根据请求参数获取命令模板，校验参数安全性后在Kubernetes容器内执行命令
     *
     * @param request 命令执行请求参数，包含模板ID、集群ID、命名空间、Pod名称、容器名称、参数等
     * @return 命令执行结果，包含执行的命令、输出内容、错误信息、执行状态
     * @throws BusinessException 模板不存在、集群不存在、命令执行失败时抛出相应异常
     */
    @Override
    public ExecuteCommandResult execute(ExecuteCommandRequest request) {
        // 1. 获取命令模板
        CommandTemplate template = templateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BusinessException(404, "命令模板不存在");
        }

        // 2. 校验命令模板安全性
        if (!commandValidator.validateCommand(template.getTemplate())) {
            throw new BusinessException(1003, "命令模板包含危险字符");
        }

        // 3. 构建最终命令，替换模板中的占位符
        String command = buildCommand(template.getTemplate(), request.getParams());

        // 4. 获取 K8S 客户端并执行
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        // 初始化结果对象
        ExecuteCommandResult result = new ExecuteCommandResult();
        result.setCommand(command);

        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {

            // 创建输出流和错误流用于存储命令执行结果
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();

            // 通过Kubernetes API在容器中执行命令
            try (ExecWatch execWatch = client.pods().inNamespace(request.getNamespace())
                    .withName(request.getPodName())
                    .inContainer(request.getContainerName())
                    .exec(command)) {

                // 读取标准输出流
                byte[] buffer = new byte[1024];
                int len;
                while ((len = execWatch.getOutput().read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                // 读取错误输出流
                while ((len = execWatch.getError().read(buffer)) != -1) {
                    error.write(buffer, 0, len);
                }
            }

            // 设置执行成功状态和输出结果
            result.setSuccess(true);
            result.setOutput(output.toString(StandardCharsets.UTF_8));
            result.setError(error.toString(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("Command execution failed", e);
            throw new BusinessException(1002, "命令执行失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 构建最终执行的命令
     * 将命令模板中的占位符替换为实际参数值，并对参数进行安全处理
     *
     * @param template 命令模板，包含形如 {paramName} 的占位符
     * @param params 参数映射表，key为参数名，value为参数值
     * @return 替换后的完整命令字符串
     */
    private String buildCommand(String template, java.util.Map<String, String> params) {
        String command = template;
        if (params != null) {
            // 遍历参数映射，替换模板中的占位符
            for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
                // 构建占位符格式 {key}
                String key = "{" + entry.getKey() + "}";
                // 对参数值进行安全处理，防止命令注入
                String value = commandValidator.sanitizeParameter(entry.getValue());
                command = command.replace(key, value);
            }
        }
        return command;
    }
}
