package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.ArthasVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * ArthasVersionMapper接口
 * 负责Arthas版本数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface ArthasVersionMapper extends BaseMapper<ArthasVersion> {
}
