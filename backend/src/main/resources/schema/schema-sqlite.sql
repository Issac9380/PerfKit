-- SQLite 数据库 Schema
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

-- 初始化默认管理员用户 (密码: admin123)
INSERT OR IGNORE INTO user (username, password, role) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN');

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
    download_url VARCHAR(500),
    recommended_arthas_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Arthas 版本管理表
CREATE TABLE IF NOT EXISTS arthas_version (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    download_url VARCHAR(500),
    compatible_jdk_versions VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- JDK/Arthas 版本映射表
CREATE TABLE IF NOT EXISTS version_mapping (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    jdk_version VARCHAR(50) NOT NULL,
    arthas_version VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    recommended INTEGER DEFAULT 0,
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

-- AI 大模型配置表
CREATE TABLE IF NOT EXISTS ai_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
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
