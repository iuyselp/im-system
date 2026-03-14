-- ============================================
-- IM 通信系统 V1.5 数据库迁移脚本
-- 执行日期：2026-03-14
-- ============================================

-- 1. 消息已读状态表
CREATE TABLE IF NOT EXISTS `im_message_read` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `read_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_user` (`message_id`, `user_id`) COMMENT '同一用户对同一消息只记录一次',
  KEY `idx_user` (`user_id`) COMMENT '用户 ID 索引',
  KEY `idx_message` (`message_id`) COMMENT '消息 ID 索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息已读状态表';

-- 2. 通话记录表（如果不存在则创建）
CREATE TABLE IF NOT EXISTS `im_call_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `call_id` varchar(64) NOT NULL COMMENT '通话 ID',
  `caller_id` bigint NOT NULL COMMENT '呼叫方 ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收方 ID（群聊时为 NULL）',
  `group_id` bigint DEFAULT NULL COMMENT '群组 ID（群聊时填写）',
  `type` varchar(20) NOT NULL COMMENT '通话类型：AUDIO-语音，VIDEO-视频',
  `duration` int DEFAULT 0 COMMENT '通话时长（秒）',
  `status` varchar(20) DEFAULT NULL COMMENT '通话状态：ANSWERED-已接听，MISSED-未接，REJECTED-拒接，CANCELLED-取消',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_call_id` (`call_id`) COMMENT '通话 ID 唯一索引',
  KEY `idx_caller` (`caller_id`) COMMENT '呼叫方 ID 索引',
  KEY `idx_receiver` (`receiver_id`) COMMENT '接收方 ID 索引',
  KEY `idx_group` (`group_id`) COMMENT '群组 ID 索引',
  KEY `idx_created` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通话记录表';

-- 3. 在消息表中添加已读相关字段（如果不存在）
ALTER TABLE `im_message_0` 
ADD COLUMN IF NOT EXISTS `read_status` tinyint DEFAULT 0 COMMENT '已读状态：0-未读，1-已读' AFTER `status`;

-- 4. 创建搜索索引（用于聊天记录搜索）
ALTER TABLE `im_message_0` 
ADD FULLTEXT INDEX `ft_content` (`content`) COMMENT '消息内容全文索引';

-- 5. 插入 STUN/TURN 服务器配置到配置表（如果存在配置表）
-- 如果没有配置表，这些信息可以放在 application.yml 中

-- ============================================
-- 数据初始化
-- ============================================

-- 示例：插入测试通话记录
-- INSERT INTO `im_call_record` (`call_id`, `caller_id`, `receiver_id`, `type`, `duration`, `status`) 
-- VALUES ('call_001', 1, 2, 'VIDEO', 120, 'ANSWERED');

-- ============================================
-- 迁移完成
-- ============================================
