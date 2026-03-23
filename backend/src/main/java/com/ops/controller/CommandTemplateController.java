package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.CommandTemplate;
import com.ops.mapper.CommandTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.debug("[CommandTemplateController.list] Enter");
        try {
            return Result.success(commandTemplateMapper.selectList(null));
        } catch (Exception e) {
            log.error("[CommandTemplateController.list] Error", e);
            throw e;
        }
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
        log.debug("[CommandTemplateController.get] Enter - id={}", id);
        try {
            return Result.success(commandTemplateMapper.selectById(id));
        } catch (Exception e) {
            log.error("[CommandTemplateController.get] Error", e);
            throw e;
        }
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
        log.debug("[CommandTemplateController.create] Enter - template={}", template);
        try {
            template.setCreatedAt(LocalDateTime.now());
            commandTemplateMapper.insert(template);
            return Result.success(template);
        } catch (Exception e) {
            log.error("[CommandTemplateController.create] Error", e);
            throw e;
        }
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
        log.debug("[CommandTemplateController.update] Enter - id={}, template={}", id, template);
        try {
            template.setId(id);
            commandTemplateMapper.updateById(template);
            return Result.success(template);
        } catch (Exception e) {
            log.error("[CommandTemplateController.update] Error", e);
            throw e;
        }
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
        log.debug("[CommandTemplateController.delete] Enter - id={}", id);
        try {
            commandTemplateMapper.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("[CommandTemplateController.delete] Error", e);
            throw e;
        }
    }
}
