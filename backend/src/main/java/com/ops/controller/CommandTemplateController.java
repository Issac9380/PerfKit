package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.CommandTemplate;
import com.ops.mapper.CommandTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis/templates")
@RequiredArgsConstructor
public class CommandTemplateController {

    private final CommandTemplateMapper commandTemplateMapper;

    @GetMapping
    public Result<List<CommandTemplate>> list() {
        return Result.success(commandTemplateMapper.selectList(null));
    }

    @GetMapping("/{id}")
    public Result<CommandTemplate> get(@PathVariable Long id) {
        return Result.success(commandTemplateMapper.selectById(id));
    }

    @PostMapping
    public Result<CommandTemplate> create(@RequestBody CommandTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        commandTemplateMapper.insert(template);
        return Result.success(template);
    }

    @PutMapping("/{id}")
    public Result<CommandTemplate> update(@PathVariable Long id, @RequestBody CommandTemplate template) {
        template.setId(id);
        commandTemplateMapper.updateById(template);
        return Result.success(template);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commandTemplateMapper.deleteById(id);
        return Result.success(null);
    }
}
