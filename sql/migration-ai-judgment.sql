-- ============================================================
-- AI 判决记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_judgment` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '判决ID',
    `source_type`     VARCHAR(20)  NOT NULL COMMENT '来源: REPORT, DISPUTE, GOODS_REVIEW',
    `source_id`       BIGINT       NOT NULL COMMENT '关联业务ID',
    `ai_model`        VARCHAR(50)  NOT NULL COMMENT 'AI模型名称',
    `confidence`      DECIMAL(3,2) NOT NULL COMMENT '置信度 0.00-1.00',
    `verdict`         VARCHAR(30)  NOT NULL COMMENT '判决: APPROVED, REJECTED, ESCALATED',
    `reasoning`       TEXT         COMMENT 'AI推理过程说明',
    `evidence`        JSON         COMMENT 'AI分析的证据摘要',
    `auto_handled`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否自动处理',
    `handler_id`      BIGINT       COMMENT '最终处理人ID(人工时)',
    `handle_note`     VARCHAR(500) COMMENT '人工处理备注',
    `handle_time`     DATETIME     COMMENT '处理时间',
    `appeal_reason`   VARCHAR(500) COMMENT '上诉原因',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING, PROCESSED, APPEALED, OVERRIDDEN',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_source` (`source_type`, `source_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI判决记录表';

-- ============================================================
-- AI 配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_config` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key`      VARCHAR(50)  NOT NULL COMMENT '配置键',
    `config_value`    VARCHAR(200) NOT NULL COMMENT '配置值',
    `description`     VARCHAR(200) COMMENT '说明',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- ============================================================
-- 初始配置数据
-- ============================================================
INSERT INTO `ai_config` (`config_key`, `config_value`, `description`) VALUES
('report_auto_review', 'true', '举报是否自动AI审核'),
('dispute_auto_arbitrate', 'true', '争议是否自动AI仲裁'),
('goods_auto_review', 'false', '商品发布是否自动AI审核(默认需手动)'),
('confidence_auto_threshold', '0.8', '自动执行置信度阈值'),
('confidence_escalate_threshold', '0.5', '转人工置信度阈值');
