package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.CommandTemplate;
import com.ops.mapper.CommandTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CommandTemplateController
 * 处理命令模板相关的HTTP请求，提供命令模板的增删改查功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/analysis/templates")
@RequiredArgsConstructor
public class CommandTemplateController {

    private final CommandTemplateMapper commandTemplateMapper;

    /**
     * 获取命令模板列表
     * 查询所有已创建的命令模板，用于前端展示和管理
     *
     * @return 返回命令模板列表的Result对象
     */
    @GetMapping
    public Result<List<CommandTemplate>> list() {
        return Result.success(commandTemplateMapper.selectList(null));
    }

    /**
     * 获取单个命令模板
     * 根据模板ID查询详细的命令模板信息
     *
     * @param id 命令模板ID
     * @return 返回命令模板详情的Result对象
     */
    @GetMapping("/{id}")
    public Result<CommandTemplate> get(@PathVariable Long id) {
        return Result.success(commandTemplateMapper.selectById(id));
    }

    /**
     * 创建命令模板
     * 添加新的命令模板到系统，包括模板名称、命令内容等属性
     *
     * @param template 命令模板对象
     * @return 返回创建成功的命令模板对象
     */
    @PostMapping
    public Result<CommandTemplate> create(@RequestBody CommandTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        commandTemplateMapper.insert(template);
        return Result.success(template);
    }

    /**
     * 更新命令模板
     * 根据ID更新已有命令模板的信息
     *
     * @param id      命令模板ID
     * @param template 新的命令模板数据
     * @return 返回更新后的命令模板对象
     */
    @PutMapping("/{id}")
    public Result<CommandTemplate> update(@PathVariable Long id, @RequestBody CommandTemplate template) {
        template.setId(id);
        commandTemplateMapper.updateById(template);
        return Result.success(template);
    }

    /**
     * 删除命令模板
     * 根据ID删除指定的命令模板
     *
     * @param id 命令模板ID
     * @return 返回操作结果的Result对象
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commandTemplateMapper.deleteById(id);
        return Result.success(null);
    }
}
