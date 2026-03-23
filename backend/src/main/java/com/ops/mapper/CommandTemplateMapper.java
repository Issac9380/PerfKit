package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.CommandTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 命令模板Mapper接口
 * 负责命令模板数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface CommandTemplateMapper extends BaseMapper<CommandTemplate> {
}
