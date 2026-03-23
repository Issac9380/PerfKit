# PerfKit 研发作业平台

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-blue" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3-orange" alt="Vue">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

## 1. 项目简介

PerfKit（研发作业平台）面向生产环境中常见问题的定位与分析，主要服务于研发/运维人员，提供日志分析、性能诊断、热部署等能力，是研发人员的问题定位利器。

### 核心功能

| 模块 | 功能描述 |
|------|----------|
| 用户认证 | 注册、登录、JWT 认证 |
| K8S 集群管理 | 多集群连接、Pod/容器管理、日志获取 |
| JDK/Arthas 版本管理 | 版本上传、版本映射、部署到容器 |
| 日志分析 | 多路径配置、AI 分析、案例库匹配 |
| 性能分析 | jstack/jmap/Arthas 命令执行 |
| 热部署 | .class/.jar 临时热部署 |
| 命令模板 | 预定义诊断命令模板 |
| AI 配置 | 支持 OpenAI/Claude/Azure/本地模型 |

---

## 2. 技术架构

### 2.1 技术栈

| 层级 | 技术选型 |
|------|----------|
| 前端 | Vue 3 + Element Plus + Vite + Pinia |
| 后端 | Spring Boot 3.2 + Java 17 |
| ORM | MyBatis Plus 3.5 |
| 数据库 | SQLite3（支持 MySQL/PostgreSQL 切换） |
| K8S Client | Fabric8 Kubernetes Client 6.12 |
| 安全 | JWT + BCrypt 密码加密 |
| 日志 | SLF4J + Logback |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Vue 前端                               │
│   (登录页 │ 集群管理 │ 日志分析 │ 性能分析 │ 热部署)        │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API
┌──────────────────────────▼──────────────────────────────────┐
│                    Spring Boot 后端                          │
├─────────────┬─────────────┬─────────────┬─────────────────┤
│  认证服务   │ K8S管理服务 │ 日志分析服务 │  性能分析服务    │
│    Auth     │  Cluster   │     Log     │   Analysis      │
├─────────────┴─────────────┴─────────────┴─────────────────┤
│                    核心服务层                                │
│   AuthService | K8sService | LogAnalysisService            │
├─────────────────────────────────────────────────────────────┤
│                    命令执行引擎 (安全校验)                    │
│        CommandValidator | TemplateExecutor | Audit          │
├─────────────────────────────────────────────────────────────┤
│                    MyBatis Plus 数据层                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
   ┌─────────┐       ┌───────────┐      ┌──────────┐
   │ SQLite  │       │  K8S API  │      │ AI 大模型 │
   │ (本地)   │       │  (远程)   │      │  (外部)   │
   └─────────┘       └───────────┘      └──────────┘
```

---

## 3. 项目结构

```
PerfKit/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/ops/
│   │   ├── common/            # 通用类（Result, Exception）
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器层
│   │   ├── dto/              # 数据传输对象
│   │   ├── entity/           # 实体类
│   │   ├── k8s/              # K8S 客户端工厂
│   │   ├── mapper/           # 数据访问层
│   │   ├── security/         # 安全组件
│   │   └── service/         # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── mapper/           # MyBatis 映射文件
│   │   ├── schema.sql        # 数据库表结构
│   │   └── application.yml   # 应用配置
│   └── pom.xml               # Maven 依赖配置
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/              # API 接口封装
│   │   ├── components/       # 公共组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia 状态管理
│   │   ├── views/            # 页面组件
│   │   ├── App.vue           # 根组件
│   │   └── main.js           # 入口文件
│   ├── index.html            # HTML 模板
│   ├── vite.config.js        # Vite 配置
│   └── package.json           # NPM 依赖配置
│
├── scripts/                   # 启停脚本
│   ├── ops.bat               # Windows 一键管理
│   ├── ops.sh                # Linux 一键管理
│   ├── start.bat / start.sh  # 启动脚本
│   ├── stop.bat / stop.sh    # 停止脚本
│   ├── restart.bat / restart.sh # 重启脚本
│   └── status.bat / status.sh   # 状态查询
│
├── docs/                     # 文档
│   └── superpowers/         # 设计文档
│
├── logs/                     # 日志目录
│
└── README.md                 # 本文件
```

---

## 4. 安装依赖

### 4.1 后端依赖

**环境要求：**
- JDK 17+
- Maven 3.8+

**构建项目：**
```bash
cd PerfKit/backend
mvn clean install
```

### 4.2 前端依赖

**环境要求：**
- Node.js 18+
- npm 9+

**安装依赖：**
```bash
cd PerfKit/frontend
npm install
```

---

## 5. 部署方式

### 5.1 一键启停（推荐）

**Windows：**
```bash
cd PerfKit/scripts
ops.bat start     # 启动前后端
ops.bat stop      # 停止服务
ops.bat restart   # 重启服务
ops.bat status   # 查看状态
```

**Linux：**
```bash
cd PerfKit/scripts
./ops.sh start
./ops.sh stop
./ops.sh restart
./ops.sh status
```

### 5.2 手动启动

**后端：**
```bash
cd PerfKit/backend
mvn spring-boot:run
# 访问: http://localhost:8080
```

**前端：**
```bash
cd PerfKit/frontend
npm run dev
# 访问: http://localhost:3000
```

### 5.3 配置说明

**环境变量（可选）：**
```bash
# JWT 配置
export JWT_SECRET=your-secret-key
export JWT_EXPIRATION=86400000

# 数据库配置（使用 MySQL 时）
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ops_platform
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your-password
```

---

## 6. 核心功能说明

### 6.1 K8S 集群管理

支持三种认证方式：
- **Kubeconfig**：上传 kubeconfig 文件
- **Token**：Bearer Token 认证
- **证书认证**：Client Cert + Client Key + CA Cert

### 6.2 日志分析

- 获取容器日志（支持批量多个 Pod）
- AI 智能分析（需配置 AI API）
- 案例库匹配（相似问题自动推荐）

### 6.3 性能分析

预置 20+ 命令模板：
- JVM 原生命令：jstack、jmap、jinfo 等
- Arthas 基础：dashboard、jvm、memory、thread
- Arthas 诊断：jad、sm、sc、classloader
- Arthas 观测：watch、trace、stack

### 6.4 热部署

- 支持 .class 和 .jar 文件上传
- 临时生效，容器重启后自动恢复
- 文件大小限制：50MB

---

## 7. API 接口概览

### 认证模块
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/auth/register | 用户注册 |
| POST | /api/v1/auth/login | 用户登录 |

### 集群管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/clusters | 获取集群列表 |
| POST | /api/v1/clusters | 添加集群 |
| DELETE | /api/v1/clusters/{id} | 删除集群 |
| POST | /api/v1/clusters/{id}/test | 测试连接 |
| GET | /api/v1/clusters/{id}/pods/{pod}/logs | 获取日志 |

### 日志分析
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/log/analyze | 分析日志 |

### 性能分析
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/analysis/templates | 命令模板列表 |
| POST | /api/v1/analysis/execute | 执行命令 |

---

## 8. 安全特性

### 8.1 命令执行安全
- 命令模板预定义，禁止自定义命令
- 参数校验过滤危险字符（`;`, `|`, `&`, `$`, `` ` `` 等）
- 审计日志记录所有执行操作

### 8.2 认证安全
- JWT Token 认证
- BCrypt 密码加密存储
- Token 过期时间可配置

---

## 9. 开发指南

### 9.1 代码规范

- 所有 Java 类添加 `@author Issac Song` 注释
- Service 层方法添加详细 JavaDoc
- Controller 层添加接口说明注释

### 9.2 运行测试

```bash
cd PerfKit/backend
mvn test
```

### 9.3 数据库初始化

首次启动自动创建 SQLite 数据库和表结构。数据库文件位于：
```
backend/data/ops-platform.db
```

---

## 10. 常见问题

**Q: 启动失败，端口被占用？**
```bash
# Windows 查看端口占用
netstat -ano | findstr "8080"
taskkill /F /PID <PID>
```

**Q: 前端无法连接后端？**
检查后端是否正常运行，确认前端 API 配置正确。

**Q: K8S 连接失败？**
检查集群认证配置是否正确，确保网络可达。

---

## 11. 许可证

MIT License

---

## 12. 联系方式

- 作者：Issac Song
- 版本：1.0.0
- 更新日期：2026-03-23
