package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.AiConfig;
import com.ops.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/config")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @GetMapping
    public Result<List<AiConfig>> list() {
        return Result.success(aiConfigService.list());
    }

    @GetMapping("/{id}")
    public Result<AiConfig> get(@PathVariable Long id) {
        return Result.success(aiConfigService.getById(id));
    }

    @GetMapping("/default")
    public Result<AiConfig> getDefault() {
        return Result.success(aiConfigService.getDefault());
    }

    @PostMapping
    public Result<AiConfig> create(@RequestBody AiConfig config) {
        return Result.success(aiConfigService.create(config));
    }

    @PutMapping("/{id}")
    public Result<AiConfig> update(@PathVariable Long id, @RequestBody AiConfig config) {
        return Result.success(aiConfigService.update(id, config));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiConfigService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        aiConfigService.setDefault(id);
        return Result.success(null);
    }
}
