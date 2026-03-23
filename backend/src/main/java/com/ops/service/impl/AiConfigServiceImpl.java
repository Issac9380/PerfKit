package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ops.entity.AiConfig;
import com.ops.mapper.AiConfigMapper;
import com.ops.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AiConfigServiceImpl 实现类
 * 实现AI配置管理的业务逻辑，包括配置的增删改查、默认配置设置等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConfigServiceImpl implements AiConfigService {

    /**
     * AiConfigMapper数据库操作类
     * 用于AI配置的持久化操作
     */
    private final AiConfigMapper aiConfigMapper;

    /**
     * 获取所有AI配置列表
     *
     * @return AI配置实体列表
     */
    @Override
    public List<AiConfig> list() {
        log.debug("[AiConfigServiceImpl.list] Enter");
        try {
            List<AiConfig> configs = aiConfigMapper.selectList(null);
            log.debug("[AiConfigServiceImpl.list] Success - count: {}", configs.size());
            return configs;
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.list] Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据ID获取AI配置
     *
     * @param id 配置ID
     * @return AI配置实体对象
     */
    @Override
    public AiConfig getById(Long id) {
        log.debug("[AiConfigServiceImpl.getById] Enter - id: {}", id);
        try {
            AiConfig config = aiConfigMapper.selectById(id);
            log.debug("[AiConfigServiceImpl.getById] Success - id: {}", id);
            return config;
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.getById] Error - id: {}, error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建新的AI配置
     * 如果设置为默认配置，则先清除其他默认配置
     *
     * @param config AI配置信息
     * @return 创建成功的配置实体对象
     */
    @Override
    public AiConfig create(AiConfig config) {
        log.debug("[AiConfigServiceImpl.create] Enter - name: {}", config.getName());
        try {
            // 如果设置为默认，先取消其他默认
            if (Boolean.TRUE.equals(config.getIsDefault())) {
                clearDefault();
            }
            // 设置默认状态为激活
            config.setStatus("ACTIVE");
            // 设置创建时间和更新时间
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            // 插入数据库
            aiConfigMapper.insert(config);
            log.debug("[AiConfigServiceImpl.create] Success - id: {}, name: {}", config.getId(), config.getName());
            return config;
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.create] Error - name: {}, error: {}", config.getName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新AI配置
     * 根据ID查找配置并更新，如果设置为默认则清除其他默认
     *
     * @param id 配置ID
     * @param config 更新后的配置信息
     * @return 更新后的配置实体对象
     * @throws RuntimeException 配置不存在时抛出异常
     */
    @Override
    public AiConfig update(Long id, AiConfig config) {
        log.debug("[AiConfigServiceImpl.update] Enter - id: {}", id);
        try {
            // 查询原配置是否存在
            AiConfig existing = aiConfigMapper.selectById(id);
            if (existing == null) {
                log.warn("[AiConfigServiceImpl.update] Config not found - id: {}", id);
                throw new RuntimeException("AI配置不存在");
            }

            // 如果设置为默认，先取消其他默认
            if (Boolean.TRUE.equals(config.getIsDefault())) {
                clearDefault();
            }

            // 设置ID和更新时间
            config.setId(id);
            config.setUpdatedAt(LocalDateTime.now());
            // 更新数据库
            aiConfigMapper.updateById(config);
            log.debug("[AiConfigServiceImpl.update] Success - id: {}", id);
            return config;
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.update] Error - id: {}, error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除AI配置
     *
     * @param id 配置ID
     */
    @Override
    public void delete(Long id) {
        log.debug("[AiConfigServiceImpl.delete] Enter - id: {}", id);
        try {
            aiConfigMapper.deleteById(id);
            log.debug("[AiConfigServiceImpl.delete] Success - id: {}", id);
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.delete] Error - id: {}, error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取默认AI配置
     * 查询状态为激活且设置为默认的配置
     *
     * @return 默认的AI配置实体，未设置返回null
     */
    @Override
    public AiConfig getDefault() {
        log.debug("[AiConfigServiceImpl.getDefault] Enter");
        try {
            LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiConfig::getIsDefault, true)
                   .eq(AiConfig::getStatus, "ACTIVE");
            AiConfig config = aiConfigMapper.selectOne(wrapper);
            log.debug("[AiConfigServiceImpl.getDefault] Success - found: {}", config != null);
            return config;
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.getDefault] Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 设置指定配置为默认
     * 先清除所有现有默认，再将指定配置设为默认
     *
     * @param id 要设为默认的配置ID
     */
    @Override
    @Transactional
    public void setDefault(Long id) {
        log.debug("[AiConfigServiceImpl.setDefault] Enter - id: {}", id);
        try {
            // 清除所有现有默认配置
            clearDefault();
            // 创建更新对象
            AiConfig config = new AiConfig();
            config.setId(id);
            config.setIsDefault(true);
            config.setUpdatedAt(LocalDateTime.now());
            // 更新数据库
            aiConfigMapper.updateById(config);
            log.debug("[AiConfigServiceImpl.setDefault] Success - id: {}", id);
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.setDefault] Error - id: {}, error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 清除所有默认配置
     * 将所有isDefault为true的配置设置为false
     */
    private void clearDefault() {
        log.debug("[AiConfigServiceImpl.clearDefault] Enter");
        try {
            LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiConfig::getIsDefault, true);
            // 创建更新实体，将isDefault设为false
            AiConfig oldDefault = new AiConfig();
            oldDefault.setIsDefault(false);
            aiConfigMapper.update(oldDefault, wrapper);
            log.debug("[AiConfigServiceImpl.clearDefault] Success");
        } catch (Exception e) {
            log.error("[AiConfigServiceImpl.clearDefault] Error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
