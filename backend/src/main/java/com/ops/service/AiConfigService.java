package com.ops.service;

import com.ops.entity.AiConfig;

import java.util.List;

/**
 * AI配置服务接口
 * 定义AI配置相关的业务逻辑，包括AI配置的增删改查、默认配置管理等功能
 * 用于管理系统中集成的AI服务配置信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface AiConfigService {

    /**
     * 获取所有AI配置列表
     * 查询系统中所有已配置的AI服务配置信息
     *
     * @return 返回所有AiConfig对象的列表
     */
    List<AiConfig> list();

    /**
     * 根据ID获取AI配置
     * 根据指定的配置ID获取对应的AI配置信息
     *
     * @param id AI配置的ID
     * @return 返回对应的AiConfig对象，如果不存在则返回null
     */
    AiConfig getById(Long id);

    /**
     * 创建新的AI配置
     * 创建一个新的AI服务配置
     *
     * @param config AI配置对象，包含配置名称、API地址、密钥等信息
     * @return 返回创建的AiConfig对象，包含完整的配置信息
     */
    AiConfig create(AiConfig config);

    /**
     * 更新已有的AI配置
     * 根据配置ID更新指定的AI服务配置信息
     *
     * @param id 要更新的配置ID
     * @param config 新的配置对象
     * @return 返回更新后的AiConfig对象
     */
    AiConfig update(Long id, AiConfig config);

    /**
     * 删除指定的AI配置
     * 根据配置ID删除指定的AI服务配置
     *
     * @param id 要删除的配置ID
     */
    void delete(Long id);

    /**
     * 获取默认AI配置
     * 获取系统当前设置的默认AI服务配置
     *
     * @return 返回默认的AiConfig对象
     */
    AiConfig getDefault();

    /**
     * 设置默认AI配置
     * 将指定的AI配置设置为系统的默认配置
     *
     * @param id 要设置为默认的配置ID
     */
    void setDefault(Long id);
}
