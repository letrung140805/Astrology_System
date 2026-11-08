-- ============================================================
-- TỬ VI AI DATABASE (CHUẨN HÓA ĐỒNG BỘ 100% VỚI JPA ENTITY)
-- MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS tuvi_ai
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tuvi_ai;

-- 1. USERS
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    role VARCHAR(30) DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_users_email (email),
    INDEX idx_users_username (username)
) ENGINE=InnoDB;

-- 2. BIRTH PROFILES (HỒ SƠ SINH)
CREATE TABLE IF NOT EXISTS birth_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    profile_name VARCHAR(150),
    full_name VARCHAR(150) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    birth_date DATE NOT NULL,
    birth_time TIME NULL,
    birth_time_unknown BOOLEAN NOT NULL DEFAULT FALSE,
    birth_city VARCHAR(100),
    birth_country VARCHAR(100) DEFAULT 'Vietnam',
    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_birth_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_birth_profiles_user (user_id),
    INDEX idx_birth_profiles_date (birth_date)
) ENGINE=InnoDB;

-- 3. ASTROLOGY CHARTS (LÁ SỐ TỬ VI)
CREATE TABLE IF NOT EXISTS astrology_charts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    birth_profile_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    chart_name VARCHAR(150),
    chart_type VARCHAR(50) NOT NULL DEFAULT 'TU_VI_DAU_SO',
    lunar_day INT,
    lunar_month INT,
    lunar_year INT,
    lunar_leap_month BOOLEAN DEFAULT FALSE,
    year_can VARCHAR(30),
    year_chi VARCHAR(30),
    menh_name VARCHAR(100),
    menh_element VARCHAR(100),
    cuc_name VARCHAR(100),
    cuc_element VARCHAR(100),
    cung_menh_branch VARCHAR(30),
    cung_than_branch VARCHAR(30),
    than_location VARCHAR(100),
    chart_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    calculation_version VARCHAR(120),
    raw_calculation_data LONGTEXT,
    access_token_hash CHAR(64) UNIQUE,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_astrology_charts_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_astrology_charts_profile
        FOREIGN KEY (birth_profile_id) REFERENCES birth_profiles(id) ON DELETE CASCADE,
    INDEX idx_charts_user (user_id),
    INDEX idx_charts_profile (birth_profile_id),
    INDEX idx_charts_token (access_token_hash)
) ENGINE=InnoDB;

-- 4. ASTROLOGY PALACES (12 CUNG VỊ)
CREATE TABLE IF NOT EXISTS astrology_palaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chart_id BIGINT NOT NULL,
    palace_order INT NOT NULL,
    position INT NOT NULL,
    palace_name VARCHAR(100) NOT NULL,
    earthly_branch VARCHAR(30),
    heavenly_stem VARCHAR(30),
    life_stage VARCHAR(50),
    tieu_han_start_age INT,
    tieu_han_end_age INT,
    interpretation TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_astrology_palaces_chart
        FOREIGN KEY (chart_id) REFERENCES astrology_charts(id) ON DELETE CASCADE,
    INDEX idx_palaces_chart (chart_id)
) ENGINE=InnoDB;

-- 5. STARS (DANH MỤC SAO)
CREATE TABLE IF NOT EXISTS stars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    vietnamese_name VARCHAR(100),
    category VARCHAR(50),
    star_group VARCHAR(100),
    star_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stars_code (code),
    INDEX idx_stars_name (name),
    INDEX idx_stars_type (star_type)
) ENGINE=InnoDB;

-- 6. CHART PALACE STARS (SAO TỌA THỦ TẠI CUNG)
CREATE TABLE IF NOT EXISTS chart_palace_stars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    palace_id BIGINT NOT NULL,
    star_id BIGINT NOT NULL,
    star_order INT DEFAULT 0,
    is_main_star BOOLEAN NOT NULL DEFAULT FALSE,
    brightness VARCHAR(50),
    transformation VARCHAR(50),
    position_type VARCHAR(50),
    interpretation TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chart_palace_stars_palace
        FOREIGN KEY (palace_id) REFERENCES astrology_palaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_chart_palace_stars_star
        FOREIGN KEY (star_id) REFERENCES stars(id) ON DELETE CASCADE,
    UNIQUE KEY uk_palace_star (palace_id, star_id),
    INDEX idx_cps_palace (palace_id),
    INDEX idx_cps_star (star_id)
) ENGINE=InnoDB;

-- 7. CONVERSATIONS (HỘI THOẠI AI TỬ VI)
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    chart_id BIGINT NULL,
    title VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversations_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_conversations_chart
        FOREIGN KEY (chart_id) REFERENCES astrology_charts(id) ON DELETE CASCADE,
    INDEX idx_conversations_user (user_id),
    INDEX idx_conversations_chart (chart_id),
    INDEX idx_conversations_updated (updated_at)
) ENGINE=InnoDB;

-- 8. MESSAGES (TIN NHẮN HỘI THOẠI)
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    content LONGTEXT NOT NULL,
    message_type VARCHAR(30) NOT NULL DEFAULT 'TEXT',
    context_used LONGTEXT,
    model_name VARCHAR(100),
    token_count INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    INDEX idx_messages_conversation (conversation_id),
    INDEX idx_messages_created_at (created_at)
) ENGINE=InnoDB;

-- 9. KNOWLEDGE DOCUMENTS (TÀI LIỆU KIẾN THỨC TỬ VI RAG)
CREATE TABLE IF NOT EXISTS knowledge_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    source VARCHAR(255),
    author VARCHAR(150),
    description TEXT,
    document_content LONGTEXT NOT NULL,
    version VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_knowledge_category (category),
    INDEX idx_knowledge_status (status)
) ENGINE=InnoDB;

-- 10. KNOWLEDGE CHUNKS (ĐOẠN TRÍ THỨC CHO TRUY VẤN RAG)
CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    chunk_title VARCHAR(255),
    chunk_content TEXT NOT NULL,
    keywords VARCHAR(500),
    embedding_reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_knowledge_chunks_doc
        FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    UNIQUE KEY uk_document_chunk (document_id, chunk_index),
    INDEX idx_chunks_document (document_id)
) ENGINE=InnoDB;

-- 11. COUPLE CHARTS (LÁ SỐ TƯƠNG HỢP CẶP ĐÔI)
CREATE TABLE IF NOT EXISTS couple_charts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    name_1 VARCHAR(150) NOT NULL,
    gender_1 VARCHAR(20) NOT NULL,
    birth_date_1 DATE NOT NULL,
    birth_time_1 TIME NULL,
    birthplace_1 VARCHAR(150),
    ban_menh_1 VARCHAR(100),
    can_chi_1 VARCHAR(50),
    name_2 VARCHAR(150) NOT NULL,
    gender_2 VARCHAR(20) NOT NULL,
    birth_date_2 DATE NOT NULL,
    birth_time_2 TIME NULL,
    birthplace_2 VARCHAR(150),
    ban_menh_2 VARCHAR(100),
    can_chi_2 VARCHAR(50),
    total_score INT NOT NULL,
    rating VARCHAR(100),
    rating_color VARCHAR(50),
    emotional_score DOUBLE,
    communication_score DOUBLE,
    financial_score DOUBLE,
    stability_score DOUBLE,
    living_together_score DOUBLE,
    emotional_interpretation TEXT,
    communication_interpretation TEXT,
    financial_interpretation TEXT,
    stability_interpretation TEXT,
    living_together_interpretation TEXT,
    conclusion TEXT,
    advice TEXT,
    access_token_hash VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_couple_charts_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_couple_charts_user (user_id),
    INDEX idx_couple_charts_created (created_at)
) ENGINE=InnoDB;

-- 9. FRIEND CHARTS (LÁ SỐ BẠN BÈ & CỘNG SỰ)
CREATE TABLE IF NOT EXISTS friend_charts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    name_1 VARCHAR(150) NOT NULL,
    gender_1 VARCHAR(20) NOT NULL,
    birth_date_1 DATE NOT NULL,
    birth_time_1 TIME NULL,
    birthplace_1 VARCHAR(150),
    ban_menh_1 VARCHAR(100),
    can_chi_1 VARCHAR(50),
    name_2 VARCHAR(150) NOT NULL,
    gender_2 VARCHAR(20) NOT NULL,
    birth_date_2 DATE NOT NULL,
    birth_time_2 TIME NULL,
    birthplace_2 VARCHAR(150),
    ban_menh_2 VARCHAR(100),
    can_chi_2 VARCHAR(50),
    total_score INT NOT NULL,
    rating VARCHAR(100),
    rating_color VARCHAR(50),
    partner_score DOUBLE,
    communication_score DOUBLE,
    vision_score DOUBLE,
    values_score DOUBLE,
    money_score DOUBLE,
    partner_interpretation TEXT,
    communication_interpretation TEXT,
    vision_interpretation TEXT,
    values_interpretation TEXT,
    money_interpretation TEXT,
    conclusion TEXT,
    advice TEXT,
    access_token_hash VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_friend_charts_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_friend_charts_user (user_id),
    INDEX idx_friend_charts_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- UPGRADE PROCEDURE FOR EXISTING DATABASES (TỰ ĐỘNG NÂNG CẤP)
-- ============================================================
DROP PROCEDURE IF EXISTS upgrade_tuvi_ai_schema;
DELIMITER //
CREATE PROCEDURE upgrade_tuvi_ai_schema()
BEGIN
    -- 1. birth_profiles
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'birth_profiles' AND COLUMN_NAME = 'user_id' AND IS_NULLABLE = 'NO') THEN
        ALTER TABLE birth_profiles MODIFY COLUMN user_id BIGINT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'birth_profiles' AND COLUMN_NAME = 'is_birth_time_unknown') THEN
        ALTER TABLE birth_profiles CHANGE COLUMN is_birth_time_unknown birth_time_unknown BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'birth_profiles' AND COLUMN_NAME = 'profile_name') THEN
        ALTER TABLE birth_profiles ADD COLUMN profile_name VARCHAR(150) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'birth_profiles' AND COLUMN_NAME = 'timezone') THEN
        ALTER TABLE birth_profiles ADD COLUMN timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'birth_profiles' AND COLUMN_NAME = 'is_primary') THEN
        ALTER TABLE birth_profiles ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;

    -- 2. astrology_charts
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'user_id' AND IS_NULLABLE = 'NO') THEN
        ALTER TABLE astrology_charts MODIFY COLUMN user_id BIGINT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'ming_palace_branch') THEN
        ALTER TABLE astrology_charts CHANGE COLUMN ming_palace_branch cung_menh_branch VARCHAR(30);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'than_palace_branch') THEN
        ALTER TABLE astrology_charts CHANGE COLUMN than_palace_branch cung_than_branch VARCHAR(30);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'generated_at') THEN
        ALTER TABLE astrology_charts ADD COLUMN generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'updated_at') THEN
        ALTER TABLE astrology_charts ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'chart_name') THEN
        ALTER TABLE astrology_charts ADD COLUMN chart_name VARCHAR(150);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'chart_type') THEN
        ALTER TABLE astrology_charts ADD COLUMN chart_type VARCHAR(50) NOT NULL DEFAULT 'TU_VI_DAU_SO';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'cuc_element') THEN
        ALTER TABLE astrology_charts ADD COLUMN cuc_element VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'chart_status') THEN
        ALTER TABLE astrology_charts ADD COLUMN chart_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_charts' AND COLUMN_NAME = 'access_token_hash') THEN
        ALTER TABLE astrology_charts ADD COLUMN access_token_hash CHAR(64) NULL;
    END IF;

    -- 3. stars
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stars' AND COLUMN_NAME = 'star_name') THEN
        ALTER TABLE stars CHANGE COLUMN star_name name VARCHAR(100) NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stars' AND COLUMN_NAME = 'code') THEN
        ALTER TABLE stars ADD COLUMN code VARCHAR(50) NULL;
        UPDATE stars SET code = UPPER(REPLACE(name, ' ', '_')) WHERE code IS NULL;
        ALTER TABLE stars MODIFY COLUMN code VARCHAR(50) NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stars' AND COLUMN_NAME = 'vietnamese_name') THEN
        ALTER TABLE stars ADD COLUMN vietnamese_name VARCHAR(100) NULL;
        UPDATE stars SET vietnamese_name = name WHERE vietnamese_name IS NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stars' AND COLUMN_NAME = 'star_group') THEN
        ALTER TABLE stars ADD COLUMN star_group VARCHAR(100) NULL;
    END IF;

    -- 4. astrology_palaces
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_palaces' AND COLUMN_NAME = 'position') THEN
        ALTER TABLE astrology_palaces ADD COLUMN position INT NULL;
        UPDATE astrology_palaces SET position = palace_order WHERE position IS NULL;
        ALTER TABLE astrology_palaces MODIFY COLUMN position INT NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_palaces' AND COLUMN_NAME = 'heavenly_stem') THEN
        ALTER TABLE astrology_palaces ADD COLUMN heavenly_stem VARCHAR(30) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_palaces' AND COLUMN_NAME = 'life_stage') THEN
        ALTER TABLE astrology_palaces ADD COLUMN life_stage VARCHAR(50) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_palaces' AND COLUMN_NAME = 'tieu_han_start_age') THEN
        ALTER TABLE astrology_palaces ADD COLUMN tieu_han_start_age INT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_palaces' AND COLUMN_NAME = 'tieu_han_end_age') THEN
        ALTER TABLE astrology_palaces ADD COLUMN tieu_han_end_age INT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'astrology_palaces' AND COLUMN_NAME = 'interpretation') THEN
        ALTER TABLE astrology_palaces ADD COLUMN interpretation TEXT NULL;
    END IF;

    -- 5. chart_palace_stars
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chart_palace_stars' AND COLUMN_NAME = 'transformation') THEN
        ALTER TABLE chart_palace_stars ADD COLUMN transformation VARCHAR(50) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chart_palace_stars' AND COLUMN_NAME = 'position_type') THEN
        ALTER TABLE chart_palace_stars ADD COLUMN position_type VARCHAR(50) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chart_palace_stars' AND COLUMN_NAME = 'interpretation') THEN
        ALTER TABLE chart_palace_stars ADD COLUMN interpretation TEXT NULL;
    END IF;

    -- 6. conversations
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'conversations' AND COLUMN_NAME = 'conversation_title') THEN
        ALTER TABLE conversations CHANGE COLUMN conversation_title title VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'conversations' AND COLUMN_NAME = 'status') THEN
        ALTER TABLE conversations ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
    END IF;

    -- 7. messages
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'messages' AND COLUMN_NAME = 'message_content') THEN
        ALTER TABLE messages CHANGE COLUMN message_content content LONGTEXT NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'messages' AND COLUMN_NAME = 'message_type') THEN
        ALTER TABLE messages ADD COLUMN message_type VARCHAR(30) NOT NULL DEFAULT 'TEXT';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'messages' AND COLUMN_NAME = 'context_used') THEN
        ALTER TABLE messages ADD COLUMN context_used LONGTEXT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'messages' AND COLUMN_NAME = 'model_name') THEN
        ALTER TABLE messages ADD COLUMN model_name VARCHAR(100) NULL;
    END IF;
END //
DELIMITER ;

CALL upgrade_tuvi_ai_schema();
DROP PROCEDURE upgrade_tuvi_ai_schema;

SHOW TABLES;
