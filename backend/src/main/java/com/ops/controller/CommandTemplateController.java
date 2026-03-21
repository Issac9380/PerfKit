package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.CommandTemplate;
import com.ops.mapper.CommandTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
