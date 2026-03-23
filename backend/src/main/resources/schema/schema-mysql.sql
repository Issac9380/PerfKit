-- MySQL 数据库 Schema
-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化默认管理员用户 (密码: admin123)
INSERT IGNORE INTO user (username, password, role) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN');

-- K8S 集群配置表
CREATE TABLE IF NOT EXISTS k8s_cluster (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    api_server VARCHAR(255) NOT NULL,
    auth_type VARCHAR(20) NOT NULL,
    config TEXT,
    description VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- JDK 版本管理表
CREATE TABLE IF NOT EXISTS jdk_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    download_url VARCHAR(500),
    recommended_arthas_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Arthas 版本管理表
CREATE TABLE IF NOT EXISTS arthas_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    md5 VARCHAR(32),
    download_url VARCHAR(500),
    compatible_jdk_versions VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- JDK/Arthas 版本映射表
CREATE TABLE IF NOT EXISTS version_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jdk_version VARCHAR(50) NOT NULL,
    arthas_version VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    recommended TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_jdk_version (jdk_version),
    INDEX idx_arthas_version (arthas_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 日志路径配置表
CREATE TABLE IF NOT EXISTS log_path_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cluster_id BIGINT,
    container_type VARCHAR(50) NOT NULL,
    log_path_pattern VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cluster_id (cluster_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 案例库表
CREATE TABLE IF NOT EXISTS case_library (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    problem_pattern VARCHAR(500),
    solution VARCHAR(1000),
    container_type VARCHAR(50),
    tags VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 日志分析记录表
CREATE TABLE IF NOT EXISTS log_analysis_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cluster_id BIGINT,
    namespace VARCHAR(100),
    pod_name VARCHAR(100),
    container_name VARCHAR(100),
    log_content LONGTEXT,
    analysis_result LONGTEXT,
    case_id BIGINT,
    ai_model VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cluster_id (cluster_id),
    INDEX idx_pod_name (pod_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 命令模板表
CREATE TABLE IF NOT EXISTS command_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    command_type VARCHAR(20) NOT NULL,
    template VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_command_type (command_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI 大模型配置表
CREATE TABLE IF NOT EXISTS ai_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    api_key VARCHAR(500),
    endpoint VARCHAR(255),
    model VARCHAR(100),
    is_default TINYINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_provider (provider),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    request_params TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 热部署记录表
CREATE TABLE IF NOT EXISTS hot_deploy_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cluster_id BIGINT,
    namespace VARCHAR(100),
    pod_name VARCHAR(100),
    container_name VARCHAR(100),
    file_name VARCHAR(255),
    class_name VARCHAR(255),
    method_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cluster_id (cluster_id),
    INDEX idx_pod_name (pod_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
