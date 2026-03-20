# 研发作业平台设计规格书

## 1. 项目概述

### 1.1 项目背景

研发作业平台面向生产环境中常见问题的定位与分析，主要服务于研发/运维出差人员，提供日志分析、性能诊断、热部署等能力，是研发人员的问题定位利器。

### 1.2 核心功能

| 模块 | 功能描述 |
|------|----------|
| 用户认证 | 注册、登录、JWT 认证 |
| K8S 集群管理 | 多集群连接、Pod/容器管理、日志获取 |
| JDK/Arthas 版本管理 | 版本上传、部署到容器 |
| 日志分析 | 多路径配置、AI 分析、案例库匹配 |
| 性能分析 | jstack/jmap/Arthas 命令执行 |
| 热部署 | .class/.jar 临时热部署 |

---

## 2. 技术架构

### 2.1 技术栈

| 层级 | 技术选型 |
|------|----------|
| 前端 | Vue 3 + Element Plus |
| 后端 | Spring Boot 3.x |
| ORM | MyBatis Plus |
| 数据库 | SQLite3（兼容 MySQL/PostgreSQL） |
| K8S Client | Fabric8 Kubernetes Client |
| 安全 | JWT + BCrypt |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Vue 前端                               │
│   (登录页 │ 集群管理 │ 日志分析 │ 性能分析 │ 热部署)          │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API / WebSocket
┌──────────────────────────▼──────────────────────────────────┐
│                    Spring Boot 后端                          │
├─────────────┬─────────────┬─────────────┬─────────────────┤
│  认证服务    │  K8S管理服务 │  日志分析服务 │   性能分析服务   │
│  AuthController | ClusterController │ LogController │ AnalysisController │
├─────────────┴─────────────┴─────────────┴─────────────────┤
│                    核心服务层                                │
│  AuthService | K8sService | LogAnalysisService | PerformanceService |
├──────────────────────────────────────────────────────────────┤
│                    命令执行引擎 (安全校验)                    │
│  CommandValidator | TemplateExecutor | AuditLogger          │
├──────────────────────────────────────────────────────────────┤
│                    MyBatis 数据层                            │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
   ┌─────────┐       ┌───────────┐      ┌──────────┐
   │ SQLite  │       │ K8S API   │      │ AI 大模型│
   │ (本地)   │       │ (远程)    │      │ (外部)   │
   └─────────┘       └───────────┘      └──────────┘
```

### 2.3 部署架构

- **平台部署位置**：K8S 集群外部（本地服务器/虚拟机）
- **K8S 连接方式**：支持多种认证方式
  - kubeconfig 文件
  - Token 认证
  - 证书认证

---

## 3. 数据库设计

### 3.1 核心表结构

```sql
-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- K8S 集群配置表
CREATE TABLE k8s_cluster (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    api_server VARCHAR(255) NOT NULL,
    auth_type VARCHAR(20) NOT NULL,
    config JSON,
    description VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- JDK 版本管理表
CREATE TABLE jdk_version (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Arthas 版本管理表
CREATE TABLE arthas_version (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 日志路径配置表
CREATE TABLE log_path_config (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    cluster_id BIGINT,
    container_type VARCHAR(50) NOT NULL,
    log_path_pattern VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 案例库表
CREATE TABLE case_library (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(200) NOT NULL,
    problem_pattern VARCHAR(500),
    solution VARCHAR(1000),
    container_type VARCHAR(50),
    tags VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 日志分析记录表
CREATE TABLE log_analysis_record (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    cluster_id BIGINT,
    namespace VARCHAR(100),
    pod_name VARCHAR(100),
    container_name VARCHAR(100),
    log_content TEXT,
    analysis_result TEXT,
    case_id BIGINT,
    ai_model VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 命令模板表
CREATE TABLE command_template (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    command_type VARCHAR(20) NOT NULL,
    template VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 操作审计日志表
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    request_params TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.2 数据库兼容

通过 MyBatis Plus 配置实现多数据库兼容：

```yaml
spring:
  datasource:
    driver-class-name: org.sqlite.JdbcDriver  # SQLite
    # driver-class-name: com.mysql.cj.jdbc.Driver  # MySQL
    # driver-class-name: org.postgresql.Driver     # PostgreSQL
```

---

## 4. API 设计

### 4.1 API 基础规范

- **基础路径**：`/api/v1`
- **认证方式**：JWT Token（Header: `Authorization: Bearer <token>`）
- **返回格式**：统一 JSON 响应

### 4.2 认证模块 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/register | 用户注册 |
| POST | /auth/login | 用户登录，返回 JWT |
| POST | /auth/refresh | 刷新 Token |
| GET | /auth/profile | 获取当前用户信息 |
| POST | /auth/logout | 登出 |

### 4.3 K8S 集群管理 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /clusters | 获取集群列表 |
| POST | /clusters | 添加集群配置 |
| GET | /clusters/{id} | 获取集群详情 |
| PUT | /clusters/{id} | 更新集群配置 |
| DELETE | /clusters/{id} | 删除集群配置 |
| POST | /clusters/{id}/test | 测试集群连接 |
| GET | /clusters/{id}/namespaces | 获取命名空间列表 |
| GET | /clusters/{id}/pods | 获取 Pod 列表 |
| GET | /clusters/{id}/pods/{pod}/containers | 获取容器列表 |
| GET | /clusters/{id}/pods/{pod}/logs | 获取容器日志 |
| POST | /clusters/{id}/exec | 在容器中执行命令 |

### 4.4 JDK/Arthas 版本管理 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /versions/jdk | 获取 JDK 版本列表 |
| POST | /versions/jdk | 上传新 JDK 版本 |
| DELETE | /versions/jdk/{id} | 删除 JDK 版本 |
| GET | /versions/arthas | 获取 Arthas 版本列表 |
| POST | /versions/arthas | 上传新 Arthas 版本 |
| DELETE | /versions/arthas/{id} | 删除 Arthas 版本 |
| POST | /versions/deploy | 部署 JDK/Arthas 到容器 |

### 4.5 日志分析 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /log/configs | 获取日志路径配置列表 |
| POST | /log/configs | 添加日志路径配置 |
| GET | /log/containers/{clusterId} | 获取可分析日志的容器 |
| POST | /log/analyze | 分析日志（AI + 案例库） |
| GET | /log/records | 获取分析记录列表 |

### 4.6 性能分析 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /analysis/templates | 获取命令模板列表 |
| POST | /analysis/execute | 执行性能分析命令 |
| GET | /analysis/pods/{clusterId} | 获取可分析的 Pod |
| POST | /analysis/batch | 批量分析多个容器 |
| GET | /analysis/result/{id} | 获取分析结果 |
| GET | /analysis/result/{id}/download | 下载分析结果文件 |

### 4.7 热部署 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /deploy/upload | 上传 .class/.jar 文件 |
| POST | /deploy/hot | 执行热部署 |
| GET | /deploy/history | 获取热部署历史 |

### 4.8 案例库 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /cases | 获取案例列表 |
| POST | /cases | 添加案例 |
| GET | /cases/{id} | 获取案例详情 |
| PUT | /cases/{id} | 更新案例 |
| DELETE | /cases/{id} | 删除案例 |

### 4.9 错误码规范

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | K8S 连接失败 |
| 1002 | 命令执行超时 |
| 1003 | 命令执行被拒绝（安全拦截） |
| 1004 | 文件上传失败 |

---

## 5. 安全设计

### 5.1 命令执行安全

采用**命令模板 + 参数校验**策略：

1. **模板定义**：预定义允许的命令模板，存储在数据库
   ```
   jstack: "jstack {pid}"
   jmap: "jmap -heap {pid}"
   arthas: "java -jar arthas-boot.jar {pid}"
   ```

2. **参数校验**：过滤危险字符
   - 禁止字符：`;`, `|`, `&`, `$()`, `` ` ``, `>`, `<`, `\n`, `\r`
   - 仅允许字母、数字、下划线、连字符、点号

3. **审计日志**：记录所有命令执行记录

### 5.2 热部署安全

- 临时生效，容器重启后自动恢复
- 限制上传文件类型：`.class`, `.jar`
- 文件大小限制：50MB

---

## 6. 扩展性设计

### 6.1 案例库扩展

当前使用 SQL 存储，预留 Elasticsearch 扩展接口：

```java
public interface CaseRepository {
    // SQL 实现
    List<Case> searchByKeyword(String keyword);

    // ES 实现（预留）
    List<Case> searchByKeywordES(String keyword);
}
```

### 6.2 AI 分析扩展

支持多 AI 模型接入：

```java
public interface AIFeedback {
    String analyze(String logContent, String model);
}

@Service
public class ClaudeAIFeedback implements AIFeedback { }

@Service
public class OpenAIAIFeedback implements AIFeedback { }
```

---

## 7. 核心流程

### 7.1 日志分析流程

```
用户选择容器
    ↓
获取日志路径配置
    ↓
从 K8S 获取日志内容
    ↓
匹配案例库（问题模式匹配）
    ↓
调用 AI 模型分析（如选择）
    ↓
生成分析报告
    ↓
保存到分析记录
```

### 7.2 性能分析流程

```
用户选择容器
    ↓
选择分析类型（jstack/jmap/arthas）
    ↓
输入参数（PID 等）
    ↓
命令模板 + 参数校验
    ↓
通过 K8S exec 执行命令
    ↓
返回结果 / 生成下载文件
```

---

## 8. 后续工作

1. 详细前端页面设计
2. 实现计划编写
3. 迭代开发

---

*文档版本：1.0*
*创建日期：2026-03-20*
