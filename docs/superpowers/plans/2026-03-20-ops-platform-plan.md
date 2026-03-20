# 研发作业平台实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

---

## 附录: DTO 定义

### DTO 1: LogAnalyzeRequest

```java
package com.ops.dto;

import lombok.Data;

@Data
public class LogAnalyzeRequest {
    private Long clusterId;
    private String namespace;
    private String podName;
    private String containerName;
    private String containerType;
    private Integer logLines = 500;
    private boolean useAI = false;
    private String aiModel = "claude";
}
```

### DTO 2: LogAnalyzeResult

```java
package com.ops.dto;

import com.ops.entity.CaseLibrary;
import lombok.Data;

@Data
public class LogAnalyzeResult {
    private String logContent;
    private CaseLibrary matchedCase;
    private String aiAnalysis;
    private String summary;
}
```

### DTO 3: ExecuteCommandRequest

```java
package com.ops.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ExecuteCommandRequest {
    private Long clusterId;
    private String namespace;
    private String podName;
    private String containerName;
    private Long templateId;
    private Map<String, String> params;
}
```

### DTO 4: ExecuteCommandResult

```java
package com.ops.dto;

import lombok.Data;

@Data
public class ExecuteCommandResult {
    private String command;
    private boolean success;
    private String output;
    private String error;
}
```

### DTO 5: ClusterRequest

```java
package com.ops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClusterRequest {
    @NotBlank(message = "集群名称不能为空")
    private String name;
    @NotBlank(message = "API Server 地址不能为空")
    private String apiServer;
    @NotBlank(message = "认证类型不能为空")
    private String authType;
    private String config;
    private String description;
}
```

---



**Goal:** 构建完整的研发作业平台，包含用户认证、K8S集群管理、JDK/Arthas版本管理、日志分析、性能分析、热部署功能

**Architecture:** 采用前后端分离架构，后端Spring Boot + MyBatis，前端Vue 3 + Element Plus，数据库SQLite，支持多数据库兼容

**Tech Stack:** Spring Boot 3.x, MyBatis Plus, Vue 3, Element Plus, Fabric8 K8S Client, JWT, SQLite

---

## 项目结构

```
ops-platform/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/ops/
│   │   ├── OpsPlatformApplication.java
│   │   ├── config/             # 配置类
│   │   ├── controller/         # REST API 控制器
│   │   ├── service/            # 业务逻辑服务
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── security/           # 安全认证
│   │   ├── k8s/                # K8S 客户端封装
│   │   └── common/             # 公共组件
│   ├── src/main/resources/
│   │   ├── mapper/             # MyBatis XML 映射
│   │   ├── application.yml
│   │   └── schema.sql          # 数据库初始化脚本
│   └── pom.xml
├── frontend/                   # Vue 前端
│   ├── src/
│   │   ├── api/                # API 调用
│   │   ├── views/              # 页面组件
│   │   ├── router/             # 路由配置
│   │   ├── store/              # 状态管理
│   │   └── components/         # 公共组件
│   ├── package.json
│   └── vite.config.js
└── docs/                       # 文档
```

---

## Phase 1: 基础设施搭建

### Task 1: 创建后端项目骨架

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/ops/OpsPlatformApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ops</groupId>
    <artifactId>ops-platform</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <fabric8.version>6.12.1</fabric8.version>
        <jjwt.version>0.12.3</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- SQLite -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.44.1.0</version>
        </dependency>

        <!-- K8S Client -->
        <dependency>
            <groupId>io.fabric8</groupId>
            <artifactId>kubernetes-client</artifactId>
            <version>${fabric8.version}</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建主启动类**

```java
package com.ops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ops.mapper")
public class OpsPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpsPlatformApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: ops-platform
  datasource:
    driver-class-name: org.sqlite.JdbcDriver
    url: jdbc:sqlite:data/ops-platform.db
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

jwt:
  secret: ops-platform-secret-key-change-in-production
  expiration: 86400000

app:
  upload:
    path: data/uploads
    max-size: 52428800
```

- [ ] **Step 4: 创建 schema.sql**

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- K8S 集群配置表
CREATE TABLE IF NOT EXISTS k8s_cluster (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    api_server VARCHAR(255) NOT NULL,
    auth_type VARCHAR(20) NOT NULL,
    config TEXT,
    description VARCHAR(255),
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- JDK 版本管理表
CREATE TABLE IF NOT EXISTS jdk_version (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Arthas 版本管理表
CREATE TABLE IF NOT EXISTS arthas_version (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 日志路径配置表
CREATE TABLE IF NOT EXISTS log_path_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cluster_id INTEGER,
    container_type VARCHAR(50) NOT NULL,
    log_path_pattern VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 案例库表
CREATE TABLE IF NOT EXISTS case_library (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(200) NOT NULL,
    problem_pattern VARCHAR(500),
    solution VARCHAR(1000),
    container_type VARCHAR(50),
    tags VARCHAR(255),
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 日志分析记录表
CREATE TABLE IF NOT EXISTS log_analysis_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cluster_id INTEGER,
    namespace VARCHAR(100),
    pod_name VARCHAR(100),
    container_name VARCHAR(100),
    log_content TEXT,
    analysis_result TEXT,
    case_id INTEGER,
    ai_model VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 命令模板表
CREATE TABLE IF NOT EXISTS command_template (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    command_type VARCHAR(20) NOT NULL,
    template VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id INTEGER,
    request_params TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 热部署记录表
CREATE TABLE IF NOT EXISTS hot_deploy_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cluster_id INTEGER,
    namespace VARCHAR(100),
    pod_name VARCHAR(100),
    container_name VARCHAR(100),
    file_name VARCHAR(255),
    class_name VARCHAR(255),
    method_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化默认命令模板
INSERT INTO command_template (name, command_type, template, description) VALUES
('JVM堆内存', 'jmap', 'jmap -heap {pid}', '查看JVM堆内存信息'),
('JVM线程堆栈', 'jstack', 'jstack {pid}', '查看JVM线程堆栈'),
('Arthas诊断', 'arthas', 'arthas-client {pid}', '使用Arthas进行诊断');
```

- [ ] **Step 5: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
cd backend
git add pom.xml src/
git commit -m "feat: create backend project skeleton with Spring Boot 3"
```

---

### Task 2: 通用响应与异常处理

**Files:**
- Create: `backend/src/main/java/com/ops/common/Result.java`
- Create: `backend/src/main/java/com/ops/common/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/ops/common/BusinessException.java`

- [ ] **Step 1: 创建统一响应类**

```java
package com.ops.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
```

- [ ] **Step 2: 创建业务异常类**

```java
package com.ops.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
```

- [ ] **Step 3: 创建全局异常处理器**

```java
package com.ops.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统内部错误");
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/ops/common/
git commit -m "feat: add common response and exception handling"
```

---

## Phase 2: 用户认证模块

### Task 3: 用户实体与 Mapper

**Files:**
- Create: `backend/src/main/java/com/ops/entity/User.java`
- Create: `backend/src/main/java/com/ops/mapper/UserMapper.java`
- Create: `backend/src/main/resources/mapper/UserMapper.xml`

- [ ] **Step 1: 创建用户实体**

```java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 UserMapper**

```java
package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 3: 创建 UserMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ops.mapper.UserMapper">
</mapper>
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/ops/entity/User.java src/main/java/com/ops/mapper/
git commit -m "feat: add User entity and mapper"
```

### Task 3.1: 创建其他 Mapper

**Files:**
- Create: `backend/src/main/java/com/ops/entity/CaseLibrary.java`
- Create: `backend/src/main/java/com/ops/entity/LogPathConfig.java`
- Create: `backend/src/main/java/com/ops/entity/CommandTemplate.java`
- Create: `backend/src/main/java/com/ops/entity/HotDeployRecord.java`
- Create: `backend/src/main/java/com/ops/mapper/CaseLibraryMapper.java`
- Create: `backend/src/main/java/com/ops/mapper/LogPathConfigMapper.java`
- Create: `backend/src/main/java/com/ops/mapper/CommandTemplateMapper.java`
- Create: `backend/src/main/java/com/ops/mapper/HotDeployRecordMapper.java`

- [ ] **Step 1: 创建案例库实体**

```java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("case_library")
public class CaseLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String problemPattern;
    private String solution;
    private String containerType;
    private String tags;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建日志路径配置实体**

```java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("log_path_config")
public class LogPathConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long clusterId;
    private String containerType;
    private String logPathPattern;
    private String description;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: 创建命令模板实体**

```java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("command_template")
public class CommandTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String commandType;
    private String template;
    private String description;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: 创建热部署记录实体**

```java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hot_deploy_record")
public class HotDeployRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long clusterId;
    private String namespace;
    private String podName;
    private String containerName;
    private String fileName;
    private String className;
    private String methodName;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 5: 创建 Mapper 接口**

```java
package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.CaseLibrary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CaseLibraryMapper extends BaseMapper<CaseLibrary> {
}
```

```java
package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.LogPathConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogPathConfigMapper extends BaseMapper<LogPathConfig> {
}
```

```java
package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.CommandTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommandTemplateMapper extends BaseMapper<CommandTemplate> {
}
```

```java
package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.HotDeployRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotDeployRecordMapper extends BaseMapper<HotDeployRecord> {
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/ops/entity/ src/main/java/com/ops/mapper/
git commit -m "feat: add additional entities and mappers"
```

---

### Task 4: JWT 安全认证

**Files:**
- Create: `backend/src/main/java/com/ops/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/ops/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/ops/security/CustomUserDetailsService.java`
- Create: `backend/src/main/java/com/ops/config/SecurityConfig.java`

- [ ] **Step 1: 创建 JWT 工具类**

```java
package com.ops.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: 创建用户详情服务**

```java
package com.ops.security;

import com.ops.entity.User;
import com.ops.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", username)
        );

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return buildUserDetails(user);
    }

    public UserDetails loadUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }
        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
```

- [ ] **Step 3: 创建 JWT 认证过滤器**

```java
package com.ops.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 4: 创建 Security 配置**

```java
package com.ops.config;

import com.ops.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/ops/security/ src/main/java/com/ops/config/SecurityConfig.java
git commit -m "feat: add JWT authentication"
```

---

### Task 5: 认证 Controller

**Files:**
- Create: `backend/src/main/java/com/ops/controller/AuthController.java`
- Create: `backend/src/main/java/com/ops/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/ops/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/ops/service/AuthService.java`
- Create: `backend/src/main/java/com/ops/service/impl/AuthServiceImpl.java`

- [ ] **Step 1: 创建 DTO**

```java
// LoginRequest.java
package com.ops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}

// RegisterRequest.java
package com.ops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度在3-50之间")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度在6-100之间")
    private String password;
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

- [ ] **Step 2: 创建 AuthService**

```java
package com.ops.service;

import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;

public interface AuthService {
    String login(LoginRequest request);
    void register(RegisterRequest request);
}
```

- [ ] **Step 3: 创建 AuthServiceImpl**

```java
package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.common.BusinessException;
import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;
import com.ops.entity.User;
import com.ops.mapper.UserMapper;
import com.ops.security.JwtTokenProvider;
import com.ops.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public String login(LoginRequest request) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(403, "账户已被禁用");
        }

        return tokenProvider.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public void register(RegisterRequest request) {
        if (userMapper.selectCount(
                new QueryWrapper<User>().eq("username", request.getUsername())
        ) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setStatus("ACTIVE");

        userMapper.insert(user);
    }
}
```

- [ ] **Step 4: 创建 AuthController**

```java
package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;
import com.ops.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return Result.success(Map.of("token", token));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }
}
```

- [ ] **Step 5: 测试认证功能**

Run: `mvn spring-boot:run`
测试:
```
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","email":"admin@example.com"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/ops/controller/AuthController.java src/main/java/com/ops/dto/ src/main/java/com/ops/service/
git commit -m "feat: add authentication controller and service"
```

---

## Phase 3: K8S 集群管理模块

### Task 6: K8S 实体与配置

**Files:**
- Create: `backend/src/main/java/com/ops/entity/K8sCluster.java`
- Create: `backend/src/main/java/com/ops/mapper/K8sClusterMapper.java`
- Create: `backend/src/main/java/com/ops/k8s/KubernetesClientFactory.java`

- [ ] **Step 1: 创建 K8sCluster 实体**

```java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("k8s_cluster")
public class K8sCluster {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String apiServer;
    private String authType;
    private String config;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String status;
}
```

- [ ] **Step 2: 创建 K8sClusterMapper**

```java
package com.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.entity.K8sCluster;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface K8sClusterMapper extends BaseMapper<K8sCluster> {
}
```

- [ ] **Step 3: 创建 K8sClusterMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ops.mapper.K8sClusterMapper">
</mapper>
```

- [ ] **Step 4: 创建 K8S Client 工厂类**

```java
package com.ops.k8s;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class KubernetesClientFactory {

    private final Map<Long, KubernetesClient> clientCache = new ConcurrentHashMap<>();

    public KubernetesClient getClient(String apiServer, String authType, String config) {
        Config configObj = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .build();

        if ("kubeconfig".equals(authType)) {
            // kubeconfig 方式
            configObj = Config.fromKubeconfig(config);
        } else if ("token".equals(authType)) {
            // Token 方式
            Map<String, String> configMap = parseJsonConfig(config);
            configObj = new ConfigBuilder()
                    .withMasterUrl(apiServer)
                    .withToken(configMap.get("token"))
                    .withTrustCerts(true)
                    .build();
        } else if ("certificate".equals(authType)) {
            // 证书方式
            Map<String, String> configMap = parseJsonConfig(config);
            configObj = new ConfigBuilder()
                    .withMasterUrl(apiServer)
                    .withClientCertData(configMap.get("clientCert"))
                    .withClientKeyData(configMap.get("clientKey"))
                    .withCaCertData(configMap.get("caCert"))
                    .withTrustCerts(true)
                    .build();
        }

        return new DefaultKubernetesClient(configObj);
    }

    private Map<String, String> parseJsonConfig(String config) {
        // 简化实现，实际应使用 Jackson 解析
        return Map.of();
    }

    public void closeClient(Long clusterId) {
        KubernetesClient client = clientCache.remove(clusterId);
        if (client != null) {
            client.close();
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/ops/entity/K8sCluster.java src/main/java/com/ops/k8s/ src/main/java/com/ops/mapper/
git commit -m "feat: add K8S cluster entity and client factory"
```

---

### Task 7: K8S 集群管理服务

**Files:**
- Create: `backend/src/main/java/com/ops/service/K8sService.java`
- Create: `backend/src/main/java/com/ops/service/impl/K8sServiceImpl.java`
- Create: `backend/src/main/java/com/ops/controller/ClusterController.java`
- Create: `backend/src/main/java/com/ops/dto/ClusterRequest.java`

- [ ] **Step 1: 创建 ClusterRequest DTO**

```java
package com.ops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClusterRequest {
    @NotBlank(message = "集群名称不能为空")
    private String name;
    @NotBlank(message = "API Server 地址不能为空")
    private String apiServer;
    @NotBlank(message = "认证类型不能为空")
    private String authType;
    private String config;
    private String description;
}
```

- [ ] **Step 2: 创建 K8sService 接口**

```java
package com.ops.service;

import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;

import java.util.List;

public interface K8sService {
    List<K8sCluster> list();
    K8sCluster create(ClusterRequest request);
    K8sCluster update(Long id, ClusterRequest request);
    void delete(Long id);
    boolean testConnection(Long id);
    List<String> getNamespaces(Long id);
    List<String> getPods(Long id, String namespace);
    List<String> getContainers(Long id, String namespace, String podName);
    String getLogs(Long id, String namespace, String podName, String containerName);
}
```

- [ ] **Step 3: 创建 K8sServiceImpl**

```java
package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.common.BusinessException;
import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.K8sClusterMapper;
import com.ops.service.K8sService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class K8sServiceImpl implements K8sService {

    private final K8sClusterMapper clusterMapper;
    private final KubernetesClientFactory clientFactory;

    @Override
    public List<K8sCluster> list() {
        return clusterMapper.selectList(null);
    }

    @Override
    public K8sCluster create(ClusterRequest request) {
        K8sCluster cluster = new K8sCluster();
        cluster.setName(request.getName());
        cluster.setApiServer(request.getApiServer());
        cluster.setAuthType(request.getAuthType());
        cluster.setConfig(request.getConfig());
        cluster.setDescription(request.getDescription());
        cluster.setStatus("ACTIVE");
        clusterMapper.insert(cluster);
        return cluster;
    }

    @Override
    public K8sCluster update(Long id, ClusterRequest request) {
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        cluster.setName(request.getName());
        cluster.setApiServer(request.getApiServer());
        cluster.setAuthType(request.getAuthType());
        cluster.setConfig(request.getConfig());
        cluster.setDescription(request.getDescription());
        clusterMapper.updateById(cluster);
        return cluster;
    }

    @Override
    public void delete(Long id) {
        clusterMapper.deleteById(id);
        clientFactory.closeClient(id);
    }

    @Override
    public boolean testConnection(Long id) {
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            client.namespaces().list();
            return true;
        } catch (Exception e) {
            log.error("K8S connection test failed", e);
            return false;
        }
    }

    @Override
    public List<String> getNamespaces(Long id) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            return client.namespaces().list().getItems().stream()
                    .map(ns -> ns.getMetadata().getName())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getPods(Long id, String namespace) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
            return pods.stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getContainers(Long id, String namespace, String podName) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
            return pod.getSpec().getContainers().stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String getLogs(Long id, String namespace, String podName, String containerName) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            return client.pods().inNamespace(namespace)
                    .withName(podName)
                    .inContainer(containerName)
                    .getLog();
        }
    }
}
```

- [ ] **Step 4: 创建 ClusterController**

```java
package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;
import com.ops.service.K8sService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final K8sService k8sService;

    @GetMapping
    public Result<List<K8sCluster>> list() {
        return Result.success(k8sService.list());
    }

    @PostMapping
    public Result<K8sCluster> create(@Valid @RequestBody ClusterRequest request) {
        return Result.success(k8sService.create(request));
    }

    @PutMapping("/{id}")
    public Result<K8sCluster> update(@PathVariable Long id, @Valid @RequestBody ClusterRequest request) {
        return Result.success(k8sService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        k8sService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/test")
    public Result<Boolean> test(@PathVariable Long id) {
        return Result.success(k8sService.testConnection(id));
    }

    @GetMapping("/{id}/namespaces")
    public Result<List<String>> getNamespaces(@PathVariable Long id) {
        return Result.success(k8sService.getNamespaces(id));
    }

    @GetMapping("/{id}/pods")
    public Result<List<String>> getPods(@PathVariable Long id, @RequestParam String namespace) {
        return Result.success(k8sService.getPods(id, namespace));
    }

    @GetMapping("/{id}/pods/{pod}/containers")
    public Result<List<String>> getContainers(
            @PathVariable Long id,
            @PathVariable String pod,
            @RequestParam String namespace) {
        return Result.success(k8sService.getContainers(id, namespace, pod));
    }

    @GetMapping("/{id}/pods/{pod}/logs")
    public Result<String> getLogs(
            @PathVariable Long id,
            @PathVariable String pod,
            @RequestParam String namespace,
            @RequestParam String container) {
        return Result.success(k8sService.getLogs(id, namespace, pod, container));
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/ops/controller/ClusterController.java src/main/java/com/ops/service/impl/K8sServiceImpl.java
git commit -m "feat: add K8S cluster management"
```

---

## Phase 4: 日志分析模块

### Task 8: 日志分析服务

**Files:**
- Create: `backend/src/main/java/com/ops/entity/LogPathConfig.java`
- Create: `backend/src/main/java/com/ops/mapper/LogPathConfigMapper.java`
- Create: `backend/src/main/java/com/ops/service/LogAnalysisService.java`
- Create: `backend/src/main/java/com/ops/service/impl/LogAnalysisServiceImpl.java`
- Create: `backend/src/main/java/com/ops/controller/LogController.java`

- [ ] **Step 1: 创建日志配置实体**

```java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("log_path_config")
public class LogPathConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long clusterId;
    private String containerType;
    private String logPathPattern;
    private String description;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建案例库实体和服务**

```java
// CaseLibrary.java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("case_library")
public class CaseLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String problemPattern;
    private String solution;
    private String containerType;
    private String tags;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 LogAnalysisService**

```java
package com.ops.service;

import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;

public interface LogAnalysisService {
    LogAnalyzeResult analyze(LogAnalyzeRequest request);
}
```

- [ ] **Step 4: 创建 LogAnalysisServiceImpl**

```java
package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.common.BusinessException;
import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;
import com.ops.entity.CaseLibrary;
import com.ops.entity.K8sCluster;
import com.ops.entity.LogPathConfig;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.CaseLibraryMapper;
import com.ops.mapper.K8sClusterMapper;
import com.ops.mapper.LogPathConfigMapper;
import com.ops.service.LogAnalysisService;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisServiceImpl implements LogAnalysisService {

    private final K8sClusterMapper clusterMapper;
    private final LogPathConfigMapper logPathConfigMapper;
    private final CaseLibraryMapper caseLibraryMapper;
    private final KubernetesClientFactory clientFactory;

    @Override
    public LogAnalyzeResult analyze(LogAnalyzeRequest request) {
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        // 获取日志内容
        String logContent = getLogContent(request);

        // 匹配案例库
        CaseLibrary matchedCase = matchCase(logContent, request.getContainerType());

        // AI 分析（预留）
        String aiAnalysis = null;
        if (request.isUseAI()) {
            aiAnalysis = callAIAnalysis(logContent, request.getAiModel());
        }

        LogAnalyzeResult result = new LogAnalyzeResult();
        result.setLogContent(logContent);
        result.setMatchedCase(matchedCase);
        result.setAiAnalysis(aiAnalysis);
        result.setSummary(generateSummary(logContent, matchedCase, aiAnalysis));

        return result;
    }

    private String getLogContent(LogAnalyzeRequest request) {
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            return client.pods().inNamespace(request.getNamespace())
                    .withName(request.getPodName())
                    .inContainer(request.getContainerName())
                    .getLog();
        } catch (Exception e) {
            log.error("Failed to get log", e);
            throw new BusinessException(1001, "获取日志失败: " + e.getMessage());
        }
    }

    private CaseLibrary matchCase(String logContent, String containerType) {
        List<CaseLibrary> cases = caseLibraryMapper.selectList(
                new QueryWrapper<CaseLibrary>()
                        .eq("container_type", containerType)
        );

        for (CaseLibrary c : cases) {
            if (c.getProblemPattern() != null && logContent.contains(c.getProblemPattern())) {
                return c;
            }
        }
        return null;
    }

    private String callAIAnalysis(String logContent, String model) {
        // TODO: 实现 AI 分析调用
        return "AI 分析功能预留";
    }

    private String generateSummary(String logContent, CaseLibrary matchedCase, String aiAnalysis) {
        StringBuilder sb = new StringBuilder();
        if (matchedCase != null) {
            sb.append("匹配案例: ").append(matchedCase.getTitle()).append("\n");
            sb.append("解决方案: ").append(matchedCase.getSolution());
        }
        return sb.toString();
    }
}
```

- [ ] **Step 5: 创建 LogController**

```java
package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;
import com.ops.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/log")
@RequiredArgsConstructor
public class LogController {

    private final LogAnalysisService logAnalysisService;

    @PostMapping("/analyze")
    public Result<LogAnalyzeResult> analyze(@RequestBody LogAnalyzeRequest request) {
        return Result.success(logAnalysisService.analyze(request));
    }

    @GetMapping("/containers/{clusterId}")
    public Result<?> getContainers(@PathVariable Long clusterId, @RequestParam String namespace) {
        // TODO: 返回可分析的容器列表
        return Result.success(List.of());
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/ops/controller/LogController.java src/main/java/com/ops/service/impl/LogAnalysisServiceImpl.java
git commit -m "feat: add log analysis service"
```

---

## Phase 5: 性能分析模块

### Task 9: 命令执行安全服务

**Files:**
- Create: `backend/src/main/java/com/ops/security/CommandValidator.java`
- Create: `backend/src/main/java/com/ops/entity/CommandTemplate.java`
- Create: `backend/src/main/java/com/ops/mapper/CommandTemplateMapper.java`

- [ ] **Step 1: 创建命令验证器**

```java
package com.ops.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CommandValidator {

    // 危险字符模式
    private static final Pattern DANGEROUS_CHARS = Pattern.compile(
            "[;|`$&><\\n\\r\\t]|\\|\\|?|&&|\\$\\(|`.*?`|>|<"
    );

    // 安全参数模式：仅允许字母、数字、下划线、连字符、点号、空格
    private static final Pattern SAFE_PARAM = Pattern.compile("^[a-zA-Z0-9_\\-./ ]+$");

    public boolean validateParameter(String param) {
        if (param == null || param.isEmpty()) {
            return true;
        }
        return SAFE_PARAM.matcher(param).matches();
    }

    public boolean validateCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        return !DANGEROUS_CHARS.matcher(command).find();
    }

    public String sanitizeParameter(String param) {
        if (param == null) {
            return "";
        }
        return param.replaceAll("[^a-zA-Z0-9_\\-./]", "");
    }
}
```

- [ ] **Step 2: 创建命令模板实体和 Mapper**

```java
// CommandTemplate.java
package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("command_template")
public class CommandTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String commandType;
    private String template;
    private String description;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/ops/security/CommandValidator.java src/main/java/com/ops/entity/CommandTemplate.java
git commit -m "feat: add command validator for security"
```

---

### Task 10: 性能分析服务

**Files:**
- Create: `backend/src/main/java/com/ops/service/PerformanceService.java`
- Create: `backend/src/main/java/com/ops/service/impl/PerformanceServiceImpl.java`
- Create: `backend/src/main/java/com/ops/controller/AnalysisController.java`

- [ ] **Step 1: 创建 PerformanceService**

```java
package com.ops.service;

import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;

public interface PerformanceService {
    ExecuteCommandResult execute(ExecuteCommandRequest request);
}
```

- [ ] **Step 2: 创建 PerformanceServiceImpl**

```java
package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.common.BusinessException;
import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;
import com.ops.entity.CommandTemplate;
import com.ops.entity.K8sCluster;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.CommandTemplateMapper;
import com.ops.mapper.K8sClusterMapper;
import com.ops.security.CommandValidator;
import com.ops.service.PerformanceService;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final K8sClusterMapper clusterMapper;
    private final CommandTemplateMapper templateMapper;
    private final KubernetesClientFactory clientFactory;
    private final CommandValidator commandValidator;

    @Override
    public ExecuteCommandResult execute(ExecuteCommandRequest request) {
        // 1. 获取命令模板
        CommandTemplate template = templateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BusinessException(404, "命令模板不存在");
        }

        // 2. 校验参数
        if (!commandValidator.validateCommand(template.getTemplate())) {
            throw new BusinessException(1003, "命令模板包含危险字符");
        }

        // 3. 构建最终命令
        String command = buildCommand(template.getTemplate(), request.getParams());

        // 4. 获取 K8S 客户端并执行
        K8sCluster cluster = clusterMapper.selectById(request.getClusterId());

        ExecuteCommandResult result = new ExecuteCommandResult();
        result.setCommand(command);

        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();

            boolean success = client.pods().inNamespace(request.getNamespace())
                    .withName(request.getPodName())
                    .inContainer(request.getContainerName())
                    .exec(command)
                    .usingListener(new ExecListener() {
                        @Override
                        public void onOpen() {}

                        @Override
                        public void onClose(int code, String reason) {}

                        @Override
                        public void onError(Throwable t) {
                            log.error("Exec error", t);
                        }
                    })
                    .join();

            result.setSuccess(success);
            result.setOutput(output.toString(StandardCharsets.UTF_8));
            result.setError(error.toString(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("Command execution failed", e);
            throw new BusinessException(1002, "命令执行失败: " + e.getMessage());
        }

        return result;
    }

    private String buildCommand(String template, java.util.Map<String, String> params) {
        String command = template;
        if (params != null) {
            for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
                String key = "{" + entry.getKey() + "}";
                String value = commandValidator.sanitizeParameter(entry.getValue());
                command = command.replace(key, value);
            }
        }
        return command;
    }
}
```

- [ ] **Step 3: 创建 AnalysisController**

```java
package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;
import com.ops.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final PerformanceService performanceService;

    @PostMapping("/execute")
    public Result<ExecuteCommandResult> execute(@RequestBody ExecuteCommandRequest request) {
        return Result.success(performanceService.execute(request));
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/ops/controller/AnalysisController.java src/main/java/com/ops/service/impl/PerformanceServiceImpl.java
git commit -m "feat: add performance analysis service"
```

---

## Phase 6: 热部署模块

### Task 11: 热部署服务

**Files:**
- Create: `backend/src/main/java/com/ops/service/HotDeployService.java`
- Create: `backend/src/main/java/com/ops/service/impl/HotDeployServiceImpl.java`
- Create: `backend/src/main/java/com/ops/controller/DeployController.java`

- [ ] **Step 1: 创建 HotDeployService**

```java
package com.ops.service;

import org.springframework.web.multipart.MultipartFile;

public interface HotDeployService {
    String uploadFile(MultipartFile file);
    void deploy(Long clusterId, String namespace, String podName,
                String containerName, String fileId, String className, String methodName);
}
```

- [ ] **Step 2: 创建 HotDeployServiceImpl**

```java
package com.ops.service.impl;

import com.ops.common.BusinessException;
import com.ops.entity.K8sCluster;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.K8sClusterMapper;
import com.ops.service.HotDeployService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotDeployServiceImpl implements HotDeployService {

    @Value("${app.upload.path}")
    private String uploadPath;

    private final KubernetesClientFactory clientFactory;
    private final K8sClusterMapper clusterMapper;

    @Override
    public String uploadFile(MultipartFile file) {
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
            (!originalFilename.endsWith(".class") && !originalFilename.endsWith(".jar"))) {
            throw new BusinessException(400, "仅支持 .class 和 .jar 文件");
        }

        // 校验文件大小
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过 50MB");
        }

        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileId = UUID.randomUUID().toString();
            String saveName = fileId + "_" + originalFilename;
            Path filePath = uploadDir.resolve(saveName);
            Files.copy(file.getInputStream(), filePath);

            return fileId;
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new BusinessException(1004, "文件上传失败");
        }
    }

    @Override
    public void deploy(Long clusterId, String namespace, String podName,
                       String containerName, String fileId, String className, String methodName) {
        // 获取集群配置
        K8sCluster cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        // 获取上传的文件路径
        String filePath = uploadPath + "/" + fileId + "_*";

        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {

            // 1. 将文件复制到容器中（通过 kubectl cp 或 exec + base64）
            // 2. 使用 Arthas redefine 命令加载新类
            // 3. 记录部署历史到数据库

            log.info("Hot deploy initiated: cluster={}, pod={}, class={}", clusterId, podName, className);
        } catch (Exception e) {
            log.error("Hot deploy failed", e);
            throw new BusinessException(500, "热部署失败: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 3: 创建 DeployController**

```java
package com.ops.controller;

import com.ops.common.Result;
import com.ops.service.HotDeployService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/deploy")
@RequiredArgsConstructor
public class DeployController {

    private final HotDeployService hotDeployService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(hotDeployService.uploadFile(file));
    }

    @PostMapping("/hot")
    public Result<Void> hotDeploy(
            @RequestParam Long clusterId,
            @RequestParam String namespace,
            @RequestParam String podName,
            @RequestParam String containerName,
            @RequestParam String fileId,
            @RequestParam String className,
            @RequestParam(required = false) String methodName) {
        hotDeployService.deploy(clusterId, namespace, podName, containerName, fileId, className, methodName);
        return Result.success();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/ops/controller/DeployController.java src/main/java/com/ops/service/impl/HotDeployServiceImpl.java
git commit -m "feat: add hot deploy service"
```

---

## 后续任务

### Task 12: JDK/Arthas 版本管理
- 创建 JDK/Arthas 版本实体和 Mapper
- 实现版本上传和部署到容器功能

### Task 13: 案例库管理
- 创建案例库 CRUD API
- 实现搜索功能

### Task 14: 前端项目搭建
- 创建 Vue 3 项目
- 集成 Element Plus
- 实现页面布局和路由

### Task 15: 前端页面开发
- 登录/注册页面
- 集群管理页面
- 日志分析页面
- 性能分析页面
- 热部署页面

---

*计划版本：1.0*
*创建日期：2026-03-20*
