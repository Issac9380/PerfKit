package com.ops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 研发作业平台启动类
 * 研发作业平台后端服务入口类
 * 提供生产环境问题诊断能力，包括：
 * - K8S集群管理
 * - 日志分析（支持AI分析）
 * - 性能分析（jstack、jmap等）
 * - 热部署（Arthas）
 * - JDK/Arthas版本管理
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@SpringBootApplication
@MapperScan("com.ops.mapper")
public class OpsPlatformApplication {
    /**
     * 应用启动入口
     * 启动Spring Boot应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OpsPlatformApplication.class, args);
    }
}
