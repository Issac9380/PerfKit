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

-- 初始化默认命令模板 (使用 INSERT OR IGNORE 防止重复)
INSERT OR IGNORE INTO command_template (name, command_type, template, description) VALUES
-- JVM 原生命令
('JVM堆内存', 'jmap', 'jmap -heap {pid}', '查看JVM堆内存信息'),
('JVM线程堆栈', 'jstack', 'jstack -l {pid}', '查看JVM线程堆栈（含锁信息）'),
('JVM堆内存转储', 'jmap', 'jmap -dump:format=b,file=heap.hprof {pid}', '导出堆内存转储文件'),
('JVM垃圾回收', 'jmap', 'jmap -gcutil {pid}', '查看垃圾回收统计信息'),

-- Arthas 基础命令
('Arthas仪表盘', 'arthas', 'dashboard', '实时数据面板（CPU、内存、线程）'),
('JVM信息', 'arthas', 'jvm', '查看JVM详细信息'),
('内存信息', 'arthas', 'memory', '查看JVM内存详情'),
('线程总览', 'arthas', 'thread', '查看所有线程状态'),
('线程详情', 'arthas', 'thread {pid}', '查看指定线程详情'),

-- Arthas 诊断命令
('反编译类', 'arthas', 'jad {className}', '反编译指定类'),
('查看方法签名', 'arthas', 'sm -d {className} {methodName}', '查看方法签名详情'),
('查看类信息', 'arthas', 'sc -d {className}', '查看类的详细信息'),
('类加载器', 'arthas', 'classloader', '查看类加载器信息'),

-- Arthas 观测命令
('观察方法调用', 'arthas', 'watch {className} {methodName} "{params,returnObj}"', '观察方法入参和返回值'),
('方法调用追踪', 'arthas', 'trace {className} {methodName}', '追踪方法调用路径和耗时'),
('方法堆栈追踪', 'arthas', 'stack {className} {methodName}', '查看方法调用堆栈'),
('监听事件', 'arthas', 'watch {className} {methodName} "@org.springframework.web.bind.annotation.RequestMapping" -x 3', '监听Spring MVC请求'),

-- Arthas 火焰图
('火焰图CPU', 'arthas', 'profiler start --event cpu', '启动CPU采样'),
('火焰图内存', 'arthas', 'profiler start --event allocations', '启动内存分配采样'),
('火焰图停止', 'arthas', 'profiler stop', '停止采样并生成火焰图'),

-- Arthas 热更新命令
('热更新类', 'arthas', 'redefine /path/to/class/{className}.class', '热更新类定义（谨慎使用）');
