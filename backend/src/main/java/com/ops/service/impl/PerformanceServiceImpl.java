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

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final K8sClusterMapper clusterMapper;
    private final CommandTemplateMapper templateMapper;
    private final KubernetesClientFactory clientFactory;
    private final CommandValidator commandValidator;

    @Override
    public ExecuteCommandResult execute(ExecuteCommandRequest request) {
        // 1. 获取命令模板
        CommandTemplate template = templateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BusinessException(404, "命令模板不存在");
        }

        // 2. 校验参数
        if (!commandValidator.validateCommand(template.getTemplate())) {
            throw new BusinessException(1003, "命令模板包含危险字符");
        }

        // 3. 构建最终命令
        String command = buildCommand(template.getTemplate(), request.getParams());

        // 4. 获取 K8S 客户端并执行
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        ExecuteCommandResult result = new ExecuteCommandResult();
        result.setCommand(command);

        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();

            try (ExecWatch execWatch = client.pods().inNamespace(request.getNamespace())
                    .withName(request.getPodName())
                    .inContainer(request.getContainerName())
                    .exec(command)) {

                // 读取输出流
                byte[] buffer = new byte[1024];
                int len;
                while ((len = execWatch.getOutput().read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                while ((len = execWatch.getError().read(buffer)) != -1) {
                    error.write(buffer, 0, len);
                }
            }

            result.setSuccess(true);
            result.setOutput(output.toString(StandardCharsets.UTF_8));
            result.setError(error.toString(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("Command execution failed", e);
            throw new BusinessException(1002, "命令执行失败: " + e.getMessage());
        }

        return result;
    }

    private String buildCommand(String template, java.util.Map<String, String> params) {
        String command = template;
        if (params != null) {
            for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
                String key = "{" + entry.getKey() + "}";
                String value = commandValidator.sanitizeParameter(entry.getValue());
                command = command.replace(key, value);
            }
        }
        return command;
    }
}
