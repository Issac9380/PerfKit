package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.VersionMapping;
import org.apache.ibatis.annotations.Mapper;

/**
 * VersionMappingMapper接口
 * 负责版本映射数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface VersionMappingMapper extends BaseMapper<VersionMapping> {
}
