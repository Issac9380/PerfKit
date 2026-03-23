package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.LogPathConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日志路径配置Mapper接口
 * 负责日志路径配置数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface LogPathConfigMapper extends BaseMapper<LogPathConfig> {
}
