package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.CaseLibrary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 案例库Mapper接口
 * 负责案例库数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface CaseLibraryMapper extends BaseMapper<CaseLibrary> {
}
