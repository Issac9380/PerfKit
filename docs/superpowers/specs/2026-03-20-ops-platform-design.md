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
    download_url VARCHAR(500),
    recommended_arthas_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Arthas 版本管理表
CREATE TABLE arthas_version (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    download_url VARCHAR(500),
    compatible_jdk_versions VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- JDK/Arthas 版本映射表
CREATE TABLE version_mapping (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    jdk_version VARCHAR(50) NOT NULL,
    arthas_version VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    recommended INTEGER DEFAULT 0,
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

-- AI 大模型配置表
CREATE TABLE ai_config (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    api_key VARCHAR(500),
    endpoint VARCHAR(255),
    model VARCHAR(100),
    is_default INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
| GET | /versions/mappings | 获取版本映射列表 |
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

### 4.9 AI 配置 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /ai/config | 获取 AI 配置列表 |
| GET | /ai/config/{id} | 获取配置详情 |
| GET | /ai/config/default | 获取默认配置 |
| POST | /ai/config | 添加 AI 配置 |
| PUT | /ai/config/{id} | 更新 AI 配置 |
| DELETE | /ai/config/{id} | 删除 AI 配置 |
| PUT | /ai/config/{id}/default | 设为默认配置 |

### 4.10 错误码规范

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

## 8. 审计日志设计

### 8.1 审计日志概述

所有后端操作都会记录审计日志，包括：
- 登录/登出
- 集群配置操作
- 日志分析操作
- 命令执行操作
- 文件上传/热部署操作

### 8.2 审计日志记录内容

| 字段 | 说明 |
|------|------|
| userId | 当前登录用户ID |
| action | 操作类型 |
| resourceType | 资源类型 (auth, cluster, log, analysis, deploy) |
| resourceId | 资源ID |
| requestParams | 请求参数（JSON格式） |
| ipAddress | 客户端IP地址 |
| createdAt | 操作时间 |

---

## 9. 命令模板设计

### 9.1 命令模板类型

| 类别 | 命令类型 | 说明 |
|------|----------|------|
| JVM原生 | jmap, jstack | JVM自带诊断工具 |
| Arthas基础 | dashboard, jvm, memory, thread | Arthas基础命令 |
| Arthas诊断 | jad, sm, sc, classloader | 类/方法诊断 |
| Arthas观测 | watch, trace, stack | 方法调用观测 |
| 火焰图 | profiler | 性能采样 |
| 热更新 | redefine | 热更新类 |

### 9.2 预设命令模板 (20+)

```
JVM 原生命令:
- jmap -heap {pid}         查看JVM堆内存信息
- jstack -l {pid}          查看JVM线程堆栈（含锁信息）
- jmap -dump:format=b,file=heap.hprof {pid}  导出堆内存转储文件
- jmap -gcutil {pid}       查看垃圾回收统计信息

Arthas 基础命令:
- dashboard                 实时数据面板
- jvm                      查看JVM详细信息
- memory                   查看JVM内存详情
- thread                   查看所有线程状态
- thread {pid}             查看指定线程详情

Arthas 诊断命令:
- jad {className}         反编译指定类
- sm -d {className} {methodName}  查看方法签名详情
- sc -d {className}       查看类的详细信息
- classloader             查看类加载器信息

Arthas 观测命令:
- watch {className} {methodName} "{params,returnObj}"  观察方法入参和返回值
- trace {className} {methodName}  追踪方法调用路径和耗时
- stack {className} {methodName}  查看方法调用堆栈

火焰图命令:
- profiler start --event cpu    启动CPU采样
- profiler start --event allocations  启动内存分配采样
- profiler stop             停止采样并生成火焰图

热更新命令:
- redefine /path/to/class/{className}.class  热更新类定义
```

---

## 10. 版本管理设计

### 10.1 JDK 版本管理

| 功能 | 说明 |
|------|------|
| 版本上传 | 支持 .tar.gz, .zip, .jdk 格式 |
| 版本存储 | 本地文件系统 + MD5校验 |
| 自动提取 | 从文件名自动提取版本号 |
| 下载链接 | 官方下载链接（可选） |
| 配套 Arthas | 推荐配套的 Arthas 版本 |
| 部署功能 | 可部署到指定容器 |

**官方下载：**
- Adoptium (推荐): https://adoptium.net/
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/

### 10.2 Arthas 版本管理

| 功能 | 说明 |
|------|------|
| 版本上传 | 支持 .jar, .zip 格式 |
| 版本存储 | 本地文件系统 + MD5校验 |
| 自动提取 | 从文件名自动提取版本号 |
| 下载链接 | 官方下载链接（可选） |
| 兼容 JDK | 兼容的 JDK 版本范围 |
| 部署功能 | 可部署到指定容器进行诊断 |

**官方下载：**
- GitHub Releases: https://github.com/alibaba/arthas/releases
- 官网: https://arthas.aliyun.com/

### 10.3 JDK/Arthas 版本映射

提供推荐的版本组合：

| JDK 版本 | Arthas 版本 | 说明 |
|----------|-------------|------|
| 8 | 3.7.2 | 推荐 |
| 11 | 3.7.2 | 推荐 |
| 17 | 3.7.2 | 推荐 |
| 21 | 3.7.2 | 推荐 |
| 8 | 3.7.1 | 可用 |
| 11 | 3.7.1 | 可用 |
| 17 | 3.7.1 | 可用 |

---

## 11. API 完整列表

### 11.1 认证模块

| 方法 | 路径 | 说明 | 审计动作 |
|------|------|------|----------|
| POST | /api/v1/auth/register | 用户注册 | REGISTER |
| POST | /api/v1/auth/login | 用户登录 | LOGIN |

### 11.2 集群管理模块

| 方法 | 路径 | 说明 | 审计动作 |
|------|------|------|----------|
| GET | /api/v1/clusters | 获取集群列表 | LIST |
| POST | /api/v1/clusters | 添加集群 | CREATE |
| GET | /api/v1/clusters/{id} | 获取集群详情 | VIEW |
| PUT | /api/v1/clusters/{id} | 更新集群 | UPDATE |
| DELETE | /api/v1/clusters/{id} | 删除集群 | DELETE |
| POST | /api/v1/clusters/{id}/test | 测试连接 | TEST_CONNECTION |
| GET | /api/v1/clusters/{id}/namespaces | 获取命名空间 | LIST_NAMESPACES |
| GET | /api/v1/clusters/{id}/pods | 获取Pod列表 | LIST_PODS |
| GET | /api/v1/clusters/{id}/pods/{pod}/containers | 获取容器列表 | LIST_CONTAINERS |
| GET | /api/v1/clusters/{id}/pods/{pod}/logs | 获取容器日志 | GET_LOGS |
| GET | /api/v1/clusters/{id}/pods/batch/logs | 批量获取多个Pod日志 | BATCH_GET_LOGS |

### 11.3 版本管理模块

| 方法 | 路径 | 说明 | 审计动作 |
|------|------|------|----------|
| GET | /api/v1/versions/jdk | 获取JDK版本列表 | LIST |
| POST | /api/v1/versions/jdk | 上传JDK版本 | UPLOAD_JDK |
| DELETE | /api/v1/versions/jdk/{id} | 删除JDK版本 | DELETE |
| GET | /api/v1/versions/arthas | 获取Arthas版本列表 | LIST |
| POST | /api/v1/versions/arthas | 上传Arthas版本 | UPLOAD_ATHAS |
| DELETE | /api/v1/versions/arthas/{id} | 删除Arthas版本 | DELETE |
| POST | /api/v1/versions/deploy | 部署到容器 | DEPLOY_VERSION |

### 11.4 日志分析模块

| 方法 | 路径 | 说明 | 审计动作 |
|------|------|------|----------|
| POST | /api/v1/log/analyze | 分析日志 | LOG_ANALYZE |
| GET | /api/v1/log/containers/{clusterId} | 获取可分析容器 | LIST_CONTAINERS |

### 11.5 性能分析模块

| 方法 | 路径 | 说明 | 审计动作 |
|------|------|------|----------|
| GET | /api/v1/analysis/templates | 获取命令模板 | LIST |
| GET | /api/v1/analysis/templates/{id} | 获取模板详情 | VIEW |
| POST | /api/v1/analysis/execute | 执行命令 | EXECUTE_COMMAND |

### 11.6 热部署模块

| 方法 | 路径 | 说明 | 审计动作 |
|------|------|------|----------|
| POST | /api/v1/deploy/upload | 上传文件 | FILE_UPLOAD |
| POST | /api/v1/deploy/hot | 执行热部署 | HOT_DEPLOY |

---

## 12. AI 大模型配置

### 12.1 支持的 AI 提供商

| 提供商 | 说明 | 默认模型 |
|--------|------|----------|
| OpenAI | OpenAI API (GPT-4, GPT-3.5) | gpt-4o |
| Anthropic (Claude) | Claude API | claude-3-5-sonnet-20241022 |
| Azure OpenAI | Azure 托管 OpenAI | gpt-4 |
| 本地模型 | Ollama 等本地部署模型 | - |

### 12.2 配置字段

| 字段 | 说明 | 必填 |
|------|------|------|
| name | 配置名称 | 是 |
| provider | AI 提供商 | 是 |
| apiKey | API Key | 是 |
| endpoint | API 端点（可选） | 否 |
| model | 模型名称 | 是 |
| isDefault | 是否设为默认 | 否 |

### 12.3 使用说明

1. 用户可在"AI 配置"页面添加多个 AI 服务配置
2. 设置默认配置后，日志分析和性能分析将自动使用默认 AI
3. 支持随时切换不同的 AI 模型进行分析

---

## 13. 前端交互设计

### 12.1 Pod 选择优化

针对大规模集群（上百甚至上千个 Pod）的场景，采用表格搜索+分页的交互方式：

#### 功能特性
- **搜索过滤**：按 Pod 名称关键字实时过滤
- **分页展示**：每页显示 20 个 Pod，支持翻页
- **批量选择**：支持多选复选框，可批量选择多个 Pod
- **批量操作**：支持批量获取日志、批量性能分析

#### 界面布局
```
┌─────────────────────────────────────────────────────────┐
│ [命名空间下拉]  [搜索 Pod 名称...]         共 158 个 Pod │
├─────────────────────────────────────────────────────────┤
│ ☐ │ Pod 名称                    │ 状态     │ 操作     │
├───┼─────────────────────────────┼──────────┼──────────│
│ ☑ │ payment-service-abc123       │ Running  │ 容器 日志│
│ ☑ │ payment-service-def456       │ Running  │ 容器 日志│
│ ☐ │ order-service-ghi789         │ Running  │ 容器 日志│
│   │ ...                          │          │          │
├─────────────────────────────────────────────────────────┤
│                    < 1 2 3 ... 8 >                      │
├─────────────────────────────────────────────────────────┤
│ 批量日志 (2)  批量分析 (2)  清空选择                     │
└─────────────────────────────────────────────────────────┘
```

#### 技术实现
- 前端：Vue 3 + Element Plus Table 组件
- 后端：新增批量日志 API，支持逗号分隔的 Pod 名称批量获取

### 12.2 K8S 认证方式

支持三种 K8S 认证方式，界面根据选择动态展示对应表单：

| 认证方式 | 所需字段 |
|----------|----------|
| Kubeconfig | kubeconfig 配置文件内容 |
| Token | Bearer Token |
| 证书认证 | Client Cert、Client Key、CA Cert（支持文件上传） |

---

## 13. 后续工作

1. 详细前端页面设计
2. 实现计划编写
3. 迭代开发

---

*文档版本：1.4*
*更新日期：2026-03-21*
*更新内容：添加 JDK/Arthas 下载链接和版本映射关系*
