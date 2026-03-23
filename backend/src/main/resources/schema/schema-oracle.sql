-- Oracle 数据库 Schema
-- 用户表
CREATE TABLE "user" (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(255) NOT NULL,
    email VARCHAR2(100),
    role VARCHAR2(20) DEFAULT 'USER',
    status VARCHAR2(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化默认管理员用户 (密码: admin123)
INSERT INTO "user" (username, password, role)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM "user" WHERE username = 'admin');

CREATE INDEX idx_user_username ON "user"(username);

-- K8S 集群配置表
CREATE TABLE k8s_cluster (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    api_server VARCHAR2(255) NOT NULL,
    auth_type VARCHAR2(20) NOT NULL,
    config CLOB,
    description VARCHAR2(255),
    created_by NUMBER(19),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR2(20) DEFAULT 'ACTIVE'
);

CREATE INDEX idx_k8s_cluster_name ON k8s_cluster(name);

-- JDK 版本管理表
CREATE TABLE jdk_version (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version VARCHAR2(50) NOT NULL,
    file_path VARCHAR2(255) NOT NULL,
    file_size NUMBER(19),
    md5 VARCHAR2(32),
    download_url VARCHAR2(500),
    recommended_arthas_version VARCHAR2(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_jdk_version_version ON jdk_version(version);

-- Arthas 版本管理表
CREATE TABLE arthas_version (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version VARCHAR2(50) NOT NULL,
    file_path VARCHAR2(255) NOT NULL,
    file_size NUMBER(19),
    md5 VARCHAR2(32),
    download_url VARCHAR2(500),
    compatible_jdk_versions VARCHAR2(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_arthas_version_version ON arthas_version(version);

-- JDK/Arthas 版本映射表
CREATE TABLE version_mapping (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jdk_version VARCHAR2(50) NOT NULL,
    arthas_version VARCHAR2(50) NOT NULL,
    description VARCHAR2(255),
    number DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_version_mapping_jdk ON version_mapping(jdk_version);
CREATE INDEX idx_version_mapping_arthas ON version_mapping(arthas_version);

-- 日志路径配置表
CREATE TABLE log_path_config (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cluster_id NUMBER(19),
    container_type VARCHAR2(50) NOT NULL,
    log_path_pattern VARCHAR2(255) NOT NULL,
    description VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_log_path_config_cluster ON log_path_config(cluster_id);

-- 案例库表
CREATE TABLE case_library (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR2(200) NOT NULL,
    problem_pattern VARCHAR2(500),
    solution VARCHAR2(1000),
    container_type VARCHAR2(50),
    tags VARCHAR2(255),
    created_by NUMBER(19),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_case_library_title ON case_library(title);

-- 日志分析记录表
CREATE TABLE log_analysis_record (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cluster_id NUMBER(19),
    namespace VARCHAR2(100),
    pod_name VARCHAR2(100),
    container_name VARCHAR2(100),
    log_content CLOB,
    analysis_result CLOB,
    case_id NUMBER(19),
    ai_model VARCHAR2(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_log_analysis_cluster ON log_analysis_record(cluster_id);
CREATE INDEX idx_log_analysis_pod ON log_analysis_record(pod_name);

-- 命令模板表
CREATE TABLE command_template (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(50) NOT NULL,
    command_type VARCHAR2(20) NOT NULL,
    template VARCHAR2(255) NOT NULL,
    description VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_command_template_type ON command_template(command_type);

-- AI 大模型配置表
CREATE TABLE ai_config (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    provider VARCHAR2(20) NOT NULL,
    api_key VARCHAR2(500),
    endpoint VARCHAR2(255),
    model VARCHAR2(100),
    is_default NUMBER(1) DEFAULT 0,
    status VARCHAR2(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_config_provider ON ai_config(provider);
CREATE INDEX idx_ai_config_default ON ai_config(is_default);

-- 操作审计日志表
CREATE TABLE audit_log (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER(19),
    action VARCHAR2(50) NOT NULL,
    resource_type VARCHAR2(50),
    resource_id NUMBER(19),
    request_params CLOB,
    ip_address VARCHAR2(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_created ON audit_log(created_at);

-- 热部署记录表
CREATE TABLE hot_deploy_record (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cluster_id NUMBER(19),
    namespace VARCHAR2(100),
    pod_name VARCHAR2(100),
    container_name VARCHAR2(100),
    file_name VARCHAR2(255),
    class_name VARCHAR2(255),
    method_name VARCHAR2(100),
    status VARCHAR2(20) DEFAULT 'SUCCESS',
    created_by NUMBER(19),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hot_deploy_cluster ON hot_deploy_record(cluster_id);
CREATE INDEX idx_hot_deploy_pod ON hot_deploy_record(pod_name);
CREATE INDEX idx_hot_deploy_status ON hot_deploy_record(status);

-- 提交事务
COMMIT;
