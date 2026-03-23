package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.AiConfig;
import com.ops.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AiConfigController
 * 处理AI配置相关的HTTP请求，包括AI配置的增删改查和默认配置设置功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/ai/config")
@RequiredArgsConstructor
@Slf4j
public class AiConfigController {

    private final AiConfigService aiConfigService;

    /**
     * 获取AI配置列表
     * 查询所有已配置的AI服务配置信息
     *
     * @return 返回AI配置列表的Result对象
     */
    @GetMapping
    public Result<List<AiConfig>> list() {
        log.debug("[AiConfigController.list] Enter");
        try {
            return Result.success(aiConfigService.list());
        } catch (Exception e) {
            log.error("[AiConfigController.list] Error", e);
            throw e;
        }
    }

    /**
     * 获取单个AI配置
     * 根据ID查询详细的AI配置信息
     *
     * @param id AI配置ID
     * @return 返回AI配置详情的Result对象
     */
    @GetMapping("/{id}")
    public Result<AiConfig> get(@PathVariable Long id) {
        log.debug("[AiConfigController.get] Enter - id={}", id);
        try {
            return Result.success(aiConfigService.getById(id));
        } catch (Exception e) {
            log.error("[AiConfigController.get] Error", e);
            throw e;
        }
    }

    /**
     * 获取默认AI配置
     * 查询当前系统默认使用的AI服务配置
     *
     * @return 返回默认AI配置对象的Result对象
     */
    @GetMapping("/default")
    public Result<AiConfig> getDefault() {
        log.debug("[AiConfigController.getDefault] Enter");
        try {
            return Result.success(aiConfigService.getDefault());
        } catch (Exception e) {
            log.error("[AiConfigController.getDefault] Error", e);
            throw e;
        }
    }

    /**
     * 创建AI配置
     * 添加新的AI服务配置，包括API密钥、模型参数等
     *
     * @param config AI配置对象
     * @return 返回创建成功的AI配置对象
     */
    @PostMapping
    public Result<AiConfig> create(@RequestBody AiConfig config) {
        log.debug("[AiConfigController.create] Enter - config={}", config);
        try {
            return Result.success(aiConfigService.create(config));
        } catch (Exception e) {
            log.error("[AiConfigController.create] Error", e);
            throw e;
        }
    }

    /**
     * 更新AI配置
     * 根据ID更新已有AI配置的信息
     *
     * @param id     AI配置ID
     * @param config 新的AI配置数据
     * @return 返回更新后的AI配置对象
     */
    @PutMapping("/{id}")
    public Result<AiConfig> update(@PathVariable Long id, @RequestBody AiConfig config) {
        log.debug("[AiConfigController.update] Enter - id={}, config={}", id, config);
        try {
            return Result.success(aiConfigService.update(id, config));
        } catch (Exception e) {
            log.error("[AiConfigController.update] Error", e);
            throw e;
        }
    }

    /**
     * 删除AI配置
     * 根据ID删除指定的AI配置
     *
     * @param id AI配置ID
     * @return 返回操作结果的Result对象
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.debug("[AiConfigController.delete] Enter - id={}", id);
        try {
            aiConfigService.delete(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("[AiConfigController.delete] Error", e);
            throw e;
        }
    }

    /**
     * 设置默认AI配置
     * 将指定的AI配置设为系统默认配置，后续请求将使用该配置
     *
     * @param id AI配置ID
     * @return 返回操作结果的Result对象
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        log.debug("[AiConfigController.setDefault] Enter - id={}", id);
        try {
            aiConfigService.setDefault(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("[AiConfigController.setDefault] Error", e);
            throw e;
        }
    }
}
