package com.ops.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.entity.CommandTemplate;
import com.ops.entity.User;
import com.ops.mapper.CommandTemplateMapper;
import com.ops.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据初始化器
 * 从 YAML 文件加载预置命令模板，实现数据与数据库解耦
 * 数据库切换时无需修改数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CommandTemplateMapper commandTemplateMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${database.init-command-templates:true}")
    private boolean initCommandTemplates;

    @Override
    public void run(String... args) throws Exception {
        if (initCommandTemplates) {
            initDefaultAdmin();
            initCommandTemplates();
        }
    }

    /**
     * 初始化默认管理员用户
     */
    private void initDefaultAdmin() {
        try {
            // 检查是否已有管理员用户
            User existingAdmin = userMapper.selectOne(
                    new QueryWrapper<User>().eq("username", "admin")
            );

            if (existingAdmin != null) {
                // 强制更新管理员密码，确保密码正确
                existingAdmin.setPassword(passwordEncoder.encode("admin123"));
                userMapper.updateById(existingAdmin);
                log.info("已更新管理员用户密码: admin");
                return;
            }

            // 创建默认管理员用户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setEmail("admin@ops.local");

            userMapper.insert(admin);
            log.info("成功初始化默认管理员用户: admin");

        } catch (Exception e) {
            log.error("初始化管理员用户失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从 YAML 加载预置命令模板
     */
    private void initCommandTemplates() {
        try {
            // 检查是否已有数据
            if (commandTemplateMapper.selectCount(null) > 0) {
                log.info("命令模板已存在，跳过初始化");
                return;
            }

            ClassPathResource resource = new ClassPathResource("command-templates.yml");
            InputStream inputStream = resource.getInputStream();

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);

            List<CommandTemplate> templates = new ArrayList<>();

            // 遍历所有分类
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String category = entry.getKey();
                List<Map<String, String>> commands = (List<Map<String, String>>) entry.getValue();

                for (Map<String, String> cmd : commands) {
                    CommandTemplate template = new CommandTemplate();
                    template.setName(cmd.get("name"));
                    template.setCommandType(cmd.get("commandType"));
                    template.setTemplate(cmd.get("template"));
                    template.setDescription(cmd.get("description"));
                    templates.add(template);
                }
            }

            // 批量插入
            if (!templates.isEmpty()) {
                for (CommandTemplate template : templates) {
                    commandTemplateMapper.insert(template);
                }
                log.info("成功初始化 {} 条命令模板", templates.size());
            }

        } catch (Exception e) {
            log.error("初始化命令模板失败: {}", e.getMessage(), e);
        }
    }
}
