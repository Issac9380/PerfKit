package com.ops.service;

/**
 * 审计日志服务接口
 * 定义审计日志相关的业务逻辑，用于记录系统中的操作行为
 * 支持追踪用户操作、资源访问、请求参数等信息，便于安全审计和问题排查
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface AuditService {
    /**
     * 记录审计日志
     * 将用户的操作行为记录到审计日志中，用于安全审计和操作追踪
     *
     * @param userId 操作用户ID
     * @param action 操作动作（如CREATE、UPDATE、DELETE、LOGIN等）
     * @param resourceType 资源类型（如CLUSTER、POD、CONFIG等）
     * @param resourceId 资源ID
     * @param requestParams 请求参数（JSON格式的请求参数，可选）
     * @param ipAddress 客户端IP地址
     */
    void log(Long userId, String action, String resourceType, Long resourceId, String requestParams, String ipAddress);
}
