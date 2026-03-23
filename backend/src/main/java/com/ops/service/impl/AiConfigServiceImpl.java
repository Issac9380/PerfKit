package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ops.entity.AiConfig;
import com.ops.mapper.AiConfigMapper;
import com.ops.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiConfigServiceImpl implements AiConfigService {

    private final AiConfigMapper aiConfigMapper;

    @Override
    public List<AiConfig> list() {
        return aiConfigMapper.selectList(null);
    }

    @Override
    public AiConfig getById(Long id) {
        return aiConfigMapper.selectById(id);
    }

    @Override
    public AiConfig create(AiConfig config) {
        // 如果设置为默认，先取消其他默认
        if (Boolean.TRUE.equals(config.getIsDefault())) {
            clearDefault();
        }
        config.setStatus("ACTIVE");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        aiConfigMapper.insert(config);
        return config;
    }

    @Override
    public AiConfig update(Long id, AiConfig config) {
        AiConfig existing = aiConfigMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("AI配置不存在");
        }

        // 如果设置为默认，先取消其他默认
        if (Boolean.TRUE.equals(config.getIsDefault())) {
            clearDefault();
        }

        config.setId(id);
        config.setUpdatedAt(LocalDateTime.now());
        aiConfigMapper.updateById(config);
        return config;
    }

    @Override
    public void delete(Long id) {
        aiConfigMapper.deleteById(id);
    }

    @Override
    public AiConfig getDefault() {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConfig::getIsDefault, true)
               .eq(AiConfig::getStatus, "ACTIVE");
        return aiConfigMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        clearDefault();
        AiConfig config = new AiConfig();
        config.setId(id);
        config.setIsDefault(true);
        config.setUpdatedAt(LocalDateTime.now());
        aiConfigMapper.updateById(config);
    }

    private void clearDefault() {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConfig::getIsDefault, true);
        AiConfig oldDefault = new AiConfig();
        oldDefault.setIsDefault(false);
        aiConfigMapper.update(oldDefault, wrapper);
    }
}
