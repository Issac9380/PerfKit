package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * AuditLogMapper接口
 * 负责审计日志数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
