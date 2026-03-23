package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.AiConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AiConfigMapper接口
 * 负责AI配置数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface AiConfigMapper extends BaseMapper<AiConfig> {
}
