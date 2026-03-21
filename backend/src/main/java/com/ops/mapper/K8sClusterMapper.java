package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.K8sCluster;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface K8sClusterMapper extends BaseMapper<K8sCluster> {
}
