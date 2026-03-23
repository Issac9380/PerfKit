package com.ops.service.impl;

import com.ops.entity.AuditLog;
import com.ops.mapper.AuditLogMapper;
import com.ops.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AuditServiceImpl 实现类
 * 实现审计日志的业务逻辑，记录用户在系统中的操作行为
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    /**
     * AuditLogMapper数据库操作类
     * 用于审计日志的持久化操作
     */
    private final AuditLogMapper auditLogMapper;

    /**
     * 记录审计日志
     * 将用户的操作行为记录到审计日志表中，用于安全审计和操作追溯
     *
     * @param userId 用户ID
     * @param action 操作动作，如CREATE、UPDATE、DELETE等
     * @param resourceType 资源类型，如Cluster、Pod、Config等
     * @param resourceId 资源ID
     * @param requestParams 请求参数JSON字符串
     * @param ipAddress 客户端IP地址
     */
    @Override
    public void log(Long userId, String action, String resourceType, Long resourceId, String requestParams, String ipAddress) {
        // 创建审计日志实体
        AuditLog auditLog = new AuditLog();
        // 设置用户信息
        auditLog.setUserId(userId);
        // 设置操作类型
        auditLog.setAction(action);
        // 设置资源类型
        auditLog.setResourceType(resourceType);
        // 设置资源ID
        auditLog.setResourceId(resourceId);
        // 设置请求参数
        auditLog.setRequestParams(requestParams);
        // 设置客户端IP
        auditLog.setIpAddress(ipAddress);
        // 持久化到数据库
        auditLogMapper.insert(auditLog);
    }
}
