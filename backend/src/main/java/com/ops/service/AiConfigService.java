package com.ops.service;

import com.ops.entity.AiConfig;

import java.util.List;

public interface AiConfigService {

    List<AiConfig> list();

    AiConfig getById(Long id);

    AiConfig create(AiConfig config);

    AiConfig update(Long id, AiConfig config);

    void delete(Long id);

    AiConfig getDefault();

    void setDefault(Long id);
}
