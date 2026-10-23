-- ============================================================
-- TỬ VI AI DATABASE
-- AI-Based Vietnamese Astrology System
-- MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS tuvi_ai
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tuvi_ai;


-- ============================================================
-- 1. USERS
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    full_name VARCHAR(150),

    gender VARCHAR(20),

    role VARCHAR(30) NOT NULL DEFAULT 'USER',

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_email (email),
    INDEX idx_users_username (username)
) ENGINE=InnoDB;


-- ============================================================
-- 2. USER SETTINGS
-- ============================================================

CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    language VARCHAR(10) NOT NULL DEFAULT 'vi',

    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',

    theme VARCHAR(20) NOT NULL DEFAULT 'light',

    daily_insight_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    email_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_settings_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- 3. BIRTH PROFILES
-- ============================================================

CREATE TABLE IF NOT EXISTS birth_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    profile_name VARCHAR(150),

    full_name VARCHAR(150) NOT NULL,

    gender VARCHAR(20) NOT NULL,

    birth_date DATE NOT NULL,

    birth_time TIME,

    birth_time_unknown BOOLEAN NOT NULL DEFAULT FALSE,

    birth_city VARCHAR(100),

    birth_country VARCHAR(100) DEFAULT 'Vietnam',

    latitude DECIMAL(10,7),

    longitude DECIMAL(10,7),

    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',

    is_primary BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_birth_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_birth_profiles_user (user_id),
    INDEX idx_birth_profiles_birth_date (birth_date)
) ENGINE=InnoDB;


-- ============================================================
-- 4. ASTROLOGY CHARTS
-- ============================================================

CREATE TABLE IF NOT EXISTS astrology_charts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    birth_profile_id BIGINT NOT NULL,

    chart_name VARCHAR(150),

    chart_type VARCHAR(50) NOT NULL DEFAULT 'TU_VI_DAU_SO',

    -- Lunar calendar information
    lunar_day INT,
    lunar_month INT,
    lunar_year INT,

    lunar_leap_month BOOLEAN DEFAULT FALSE,

    -- Can Chi
    year_can VARCHAR(30),
    year_chi VARCHAR(30),

    month_can VARCHAR(30),
    month_chi VARCHAR(30),

    day_can VARCHAR(30),
    day_chi VARCHAR(30),

    hour_can VARCHAR(30),
    hour_chi VARCHAR(30),

    -- Core astrology information
    yin_yang VARCHAR(30),

    menh_name VARCHAR(100),
    menh_element VARCHAR(100),

    cuc_name VARCHAR(100),
    cuc_element VARCHAR(100),

    cung_menh_branch VARCHAR(30),
    cung_than_branch VARCHAR(30),

    than_location VARCHAR(100),

    chart_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',

    calculation_version VARCHAR(50),

    raw_calculation_data LONGTEXT,

    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_charts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_charts_birth_profile
        FOREIGN KEY (birth_profile_id)
        REFERENCES birth_profiles(id)
        ON DELETE CASCADE,

    INDEX idx_charts_user (user_id),
    INDEX idx_charts_birth_profile (birth_profile_id),
    INDEX idx_charts_generated_at (generated_at)
) ENGINE=InnoDB;


-- ============================================================
-- 5. ASTROLOGY PALACES
-- ============================================================

CREATE TABLE IF NOT EXISTS astrology_palaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    chart_id BIGINT NOT NULL,

    palace_order INT NOT NULL,

    palace_name VARCHAR(100) NOT NULL,

    earthly_branch VARCHAR(30),

    heavenly_stem VARCHAR(30),

    life_stage VARCHAR(50),

    tieu_han_start_age INT,

    tieu_han_end_age INT,

    interpretation TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_palaces_chart
        FOREIGN KEY (chart_id)
        REFERENCES astrology_charts(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_chart_palace_order
        (chart_id, palace_order),

    INDEX idx_palaces_chart (chart_id),
    INDEX idx_palaces_name (palace_name)
) ENGINE=InnoDB;


-- ============================================================
-- 6. STARS
-- ============================================================

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

    INDEX idx_stars_category (category),
    INDEX idx_stars_type (star_type),
    INDEX idx_stars_name (name)
) ENGINE=InnoDB;


-- ============================================================
-- 7. CHART PALACE STARS
-- ============================================================

CREATE TABLE IF NOT EXISTS chart_palace_stars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    palace_id BIGINT NOT NULL,

    star_id BIGINT NOT NULL,

    star_order INT,

    is_main_star BOOLEAN NOT NULL DEFAULT FALSE,

    brightness VARCHAR(50),

    transformation VARCHAR(50),

    position_type VARCHAR(50),

    interpretation TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chart_palace_stars_palace
        FOREIGN KEY (palace_id)
        REFERENCES astrology_palaces(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_chart_palace_stars_star
        FOREIGN KEY (star_id)
        REFERENCES stars(id)
        ON DELETE RESTRICT,

    UNIQUE KEY uk_palace_star
        (palace_id, star_id),

    INDEX idx_cps_palace (palace_id),
    INDEX idx_cps_star (star_id)
) ENGINE=InnoDB;


-- ============================================================
-- 8. LIFE CYCLES / ĐẠI VẬN
-- ============================================================

CREATE TABLE IF NOT EXISTS life_cycles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    chart_id BIGINT NOT NULL,

    cycle_number INT NOT NULL,

    start_age INT NOT NULL,

    end_age INT NOT NULL,

    palace_name VARCHAR(100),

    earthly_branch VARCHAR(30),

    major_stars TEXT,

    score DECIMAL(5,2),

    summary TEXT,

    detailed_interpretation TEXT,

    is_current BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_life_cycles_chart
        FOREIGN KEY (chart_id)
        REFERENCES astrology_charts(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_chart_cycle
        (chart_id, cycle_number),

    INDEX idx_life_cycles_chart (chart_id),
    INDEX idx_life_cycles_current (is_current)
) ENGINE=InnoDB;


-- ============================================================
-- 9. ASTROLOGY READINGS
-- ============================================================

CREATE TABLE IF NOT EXISTS astrology_readings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    chart_id BIGINT NOT NULL,

    section_type VARCHAR(50) NOT NULL,

    title VARCHAR(200),

    content LONGTEXT NOT NULL,

    ai_generated BOOLEAN NOT NULL DEFAULT FALSE,

    model_name VARCHAR(100),

    prompt_version VARCHAR(50),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_readings_chart
        FOREIGN KEY (chart_id)
        REFERENCES astrology_charts(id)
        ON DELETE CASCADE,

    INDEX idx_readings_chart (chart_id),
    INDEX idx_readings_section (section_type)
) ENGINE=InnoDB;


-- ============================================================
-- 10. CONVERSATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    chart_id BIGINT,

    title VARCHAR(255),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversations_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_conversations_chart
        FOREIGN KEY (chart_id)
        REFERENCES astrology_charts(id)
        ON DELETE SET NULL,

    INDEX idx_conversations_user (user_id),
    INDEX idx_conversations_chart (chart_id),
    INDEX idx_conversations_updated (updated_at)
) ENGINE=InnoDB;


-- ============================================================
-- 11. MESSAGES
-- ============================================================

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
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE CASCADE,

    INDEX idx_messages_conversation (conversation_id),
    INDEX idx_messages_created_at (created_at)
) ENGINE=InnoDB;


-- ============================================================
-- 12. KNOWLEDGE DOCUMENTS
-- ============================================================

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

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_knowledge_category (category),
    INDEX idx_knowledge_status (status),

    FULLTEXT KEY ft_knowledge_content
        (title, description, document_content)
) ENGINE=InnoDB;


-- ============================================================
-- 13. KNOWLEDGE CHUNKS
-- ============================================================

CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    document_id BIGINT NOT NULL,

    chunk_index INT NOT NULL,

    chunk_title VARCHAR(255),

    chunk_content TEXT NOT NULL,

    keywords VARCHAR(500),

    embedding_reference VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chunks_document
        FOREIGN KEY (document_id)
        REFERENCES knowledge_documents(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_document_chunk
        (document_id, chunk_index),

    INDEX idx_chunks_document (document_id),

    FULLTEXT KEY ft_chunk_content
        (chunk_title, chunk_content, keywords)
) ENGINE=InnoDB;


-- ============================================================
-- 14. DAILY INSIGHTS
-- ============================================================

CREATE TABLE IF NOT EXISTS daily_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    chart_id BIGINT NOT NULL,

    insight_date DATE NOT NULL,

    day_can VARCHAR(30),

    day_chi VARCHAR(30),

    month_can VARCHAR(30),

    month_chi VARCHAR(30),

    year_can VARCHAR(30),

    year_chi VARCHAR(30),

    wisdom_score DECIMAL(5,2),

    finance_score DECIMAL(5,2),

    relationship_score DECIMAL(5,2),

    career_score DECIMAL(5,2),

    overall_score DECIMAL(5,2),

    lucky_stars TEXT,

    recommended_actions TEXT,

    avoid_actions TEXT,

    insight_content TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_daily_insights_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_daily_insights_chart
        FOREIGN KEY (chart_id)
        REFERENCES astrology_charts(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_user_daily_insight
        (user_id, insight_date),

    INDEX idx_daily_insights_date (insight_date),
    INDEX idx_daily_insights_chart (chart_id)
) ENGINE=InnoDB;


-- ============================================================
-- 15. SUBSCRIPTION PLANS
-- ============================================================

CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    plan_code VARCHAR(50) NOT NULL UNIQUE,

    plan_name VARCHAR(100) NOT NULL,

    description TEXT,

    max_charts INT DEFAULT 10,

    max_ai_messages INT DEFAULT 100,

    daily_insight_enabled BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- ============================================================
-- 16. USER SUBSCRIPTIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS user_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    plan_id BIGINT NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_subscriptions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_subscriptions_plan
        FOREIGN KEY (plan_id)
        REFERENCES subscription_plans(id)
        ON DELETE RESTRICT,

    INDEX idx_user_subscription_user (user_id),
    INDEX idx_user_subscription_status (status)
) ENGINE=InnoDB;


-- ============================================================
-- 17. CHART SHARES
-- ============================================================

CREATE TABLE IF NOT EXISTS chart_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    chart_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    share_token VARCHAR(100) NOT NULL UNIQUE,

    expires_at TIMESTAMP NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chart_shares_chart
        FOREIGN KEY (chart_id)
        REFERENCES astrology_charts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_chart_shares_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_chart_shares_token (share_token),
    INDEX idx_chart_shares_chart (chart_id)
);

SHOW TABLES;