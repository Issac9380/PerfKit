package com.ops.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库配置属性
 * 支持配置多种数据库类型：mysql, oracle, sqlite
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "database")
public class DatabaseDialect {

    /**
     * 数据库类型: mysql, oracle, sqlite
     */
    private String type = "sqlite";

    /**
     * 获取数据库方言标识
     */
    public String getDialect() {
        return type.toLowerCase();
    }

    /**
     * 判断是否为 MySQL
     */
    public boolean isMySQL() {
        return "mysql".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为 Oracle
     */
    public boolean isOracle() {
        return "oracle".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为 SQLite
     */
    public boolean isSQLite() {
        return "sqlite".equalsIgnoreCase(type);
    }
}
