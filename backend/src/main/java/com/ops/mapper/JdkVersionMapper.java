package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.JdkVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * JdkVersionMapper接口
 * 负责JDK版本数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface JdkVersionMapper extends BaseMapper<JdkVersion> {
}
