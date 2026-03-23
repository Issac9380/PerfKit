package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.HotDeployRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * HotDeployRecordMapper接口
 * 负责热部署记录数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface HotDeployRecordMapper extends BaseMapper<HotDeployRecord> {
}
