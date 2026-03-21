package com.ops.service;

public interface AuditService {
    void log(Long userId, String action, String resourceType, Long resourceId, String requestParams, String ipAddress);
}
