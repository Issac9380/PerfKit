package com.ops.service.impl;

import com.ops.entity.AuditLog;
import com.ops.mapper.AuditLogMapper;
import com.ops.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogMapper auditLogMapper;

    @Override
    public void log(Long userId, String action, String resourceType, Long resourceId, String requestParams, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setRequestParams(requestParams);
        auditLog.setIpAddress(ipAddress);
        auditLogMapper.insert(auditLog);
    }
}
