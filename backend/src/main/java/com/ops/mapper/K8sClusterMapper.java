package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.K8sCluster;
import org.apache.ibatis.annotations.Mapper;

/**
 * K8sClusterMapper接口
 * 负责K8s集群数据的数据库操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Mapper
public interface K8sClusterMapper extends BaseMapper<K8sCluster> {
}
