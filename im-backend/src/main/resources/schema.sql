-- IM 通信系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS im_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE im_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `im_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码 (BCrypt 加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像 URL',
  `gender` tinyint(4) DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `signature` varchar(200) DEFAULT NULL COMMENT '个性签名',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
  `online_status` varchar(20) DEFAULT 'offline' COMMENT '在线状态',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录 IP',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户 Token 表
CREATE TABLE IF NOT EXISTS `im_user_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `token` varchar(255) NOT NULL COMMENT 'JWT Token',
  `refresh_token` varchar(255) DEFAULT NULL COMMENT '刷新 Token',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备 ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(20) DEFAULT 'web' COMMENT '设备类型',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP 地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `is_valid` tinyint(4) DEFAULT 1 COMMENT '是否有效',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_user` (`user_id`),
  KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户 Token 表';

-- 好友关系表
CREATE TABLE IF NOT EXISTS `im_friend` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `friend_id` bigint(20) NOT NULL COMMENT '好友 ID',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注名',
  `group_id` bigint(20) DEFAULT NULL COMMENT '好友分组 ID',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-好友 0-拉黑',
  `is_star` tinyint(4) DEFAULT 0 COMMENT '是否星标',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_friend` (`friend_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 好友申请表
CREATE TABLE IF NOT EXISTS `im_friend_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint(20) NOT NULL COMMENT '申请人 ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '被申请人 ID',
  `message` varchar(200) DEFAULT NULL COMMENT '申请消息',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态 0-待处理 1-同意 2-拒绝',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_from` (`from_user_id`),
  KEY `idx_to` (`to_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

-- 群组表
CREATE TABLE IF NOT EXISTS `im_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '群名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '群头像 URL',
  `owner_id` bigint(20) NOT NULL COMMENT '群主 ID',
  `notice` text COMMENT '群公告',
  `notice_updated_by` bigint(20) DEFAULT NULL,
  `notice_updated_at` datetime DEFAULT NULL,
  `max_members` int(11) DEFAULT 500 COMMENT '最大成员数',
  `member_count` int(11) DEFAULT 0 COMMENT '当前成员数',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-正常 0-解散',
  `is_muted` tinyint(4) DEFAULT 0 COMMENT '是否全员禁言',
  `join_type` tinyint(4) DEFAULT 0 COMMENT '加入方式 0-自由 1-审核 2-禁止',
  `chat_type` tinyint(4) DEFAULT 0 COMMENT '聊天类型 0-公开 1-私密',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

-- 群成员表
CREATE TABLE IF NOT EXISTS `im_group_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL COMMENT '群 ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `role` tinyint(4) DEFAULT 0 COMMENT '角色 0-成员 1-管理员 2-群主',
  `alias` varchar(50) DEFAULT NULL COMMENT '群昵称',
  `mute_until` datetime DEFAULT NULL COMMENT '禁言截止时间',
  `is_muted` tinyint(4) DEFAULT 0 COMMENT '是否免打扰',
  `joined_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `joined_by` bigint(20) DEFAULT NULL COMMENT '邀请人 ID',
  `deleted` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员表';

-- 消息表 00
CREATE TABLE IF NOT EXISTS `im_message_00` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `msg_id` varchar(64) NOT NULL COMMENT '消息唯一 ID',
  `from_id` bigint(20) NOT NULL COMMENT '发送者 ID',
  `to_id` bigint(20) DEFAULT NULL COMMENT '接收者 ID',
  `group_id` bigint(20) DEFAULT NULL COMMENT '群 ID',
  `type` varchar(20) NOT NULL COMMENT '消息类型',
  `sub_type` varchar(20) DEFAULT NULL COMMENT '消息子类型',
  `content` text NOT NULL COMMENT '消息内容',
  `extra` json DEFAULT NULL COMMENT '扩展数据',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-正常 0-撤回 2-删除',
  `is_read` tinyint(4) DEFAULT 0 COMMENT '是否已读',
  `read_count` int(11) DEFAULT 0 COMMENT '已读人数',
  `delivery_status` varchar(20) DEFAULT 'sent' COMMENT '投递状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_id` (`msg_id`),
  KEY `idx_from` (`from_id`),
  KEY `idx_to` (`to_id`),
  KEY `idx_group` (`group_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 创建 99 张消息分表（简化脚本，实际使用时需要创建 im_message_00 ~ im_message_99）
-- 这里只创建一张表示例

-- 会话表
CREATE TABLE IF NOT EXISTS `im_conversation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `target_id` bigint(20) NOT NULL COMMENT '目标 ID',
  `type` varchar(20) NOT NULL COMMENT '会话类型 single/group',
  `last_msg_id` bigint(20) DEFAULT NULL,
  `last_msg_content` varchar(500) DEFAULT NULL,
  `last_msg_time` datetime DEFAULT NULL,
  `unread_count` int(11) DEFAULT 0,
  `is_top` tinyint(4) DEFAULT 0,
  `is_muted` tinyint(4) DEFAULT 0,
  `is_deleted` tinyint(4) DEFAULT 0,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`),
  KEY `idx_last_msg_time` (`last_msg_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 通话记录表
CREATE TABLE IF NOT EXISTS `im_call_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `call_id` varchar(64) NOT NULL,
  `caller_id` bigint(20) NOT NULL,
  `caller_name` varchar(100) DEFAULT NULL,
  `receiver_id` bigint(20) DEFAULT NULL,
  `receiver_name` varchar(100) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `type` varchar(20) NOT NULL COMMENT 'AUDIO/VIDEO',
  `mode` varchar(20) DEFAULT '1v1',
  `duration` int(11) DEFAULT 0,
  `status` varchar(20) DEFAULT 'completed',
  `recording_url` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `ended_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_call_id` (`call_id`),
  KEY `idx_caller` (`caller_id`),
  KEY `idx_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通话记录表';

-- 客服工单表
CREATE TABLE IF NOT EXISTS `im_ticket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ticket_no` varchar(32) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  `agent_id` bigint(20) DEFAULT NULL,
  `agent_name` varchar(100) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `description` text,
  `status` varchar(20) DEFAULT 'open',
  `priority` varchar(20) DEFAULT 'normal',
  `source` varchar(20) DEFAULT 'web',
  `category` varchar(50) DEFAULT NULL,
  `rating` tinyint(4) DEFAULT NULL,
  `rating_content` varchar(500) DEFAULT NULL,
  `first_response_time` int(11) DEFAULT NULL,
  `resolution_time` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `closed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服工单表';

-- 工单消息表
CREATE TABLE IF NOT EXISTS `im_ticket_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ticket_id` bigint(20) NOT NULL,
  `from_id` bigint(20) NOT NULL,
  `from_type` varchar(20) NOT NULL,
  `type` varchar(20) NOT NULL,
  `content` text NOT NULL,
  `extra` json DEFAULT NULL,
  `is_read` tinyint(4) DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ticket` (`ticket_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单消息表';

-- 客服表
CREATE TABLE IF NOT EXISTS `im_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `nickname` varchar(100) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'offline',
  `max_concurrent` int(11) DEFAULT 5,
  `current_concurrent` int(11) DEFAULT 0,
  `skill_groups` json DEFAULT NULL,
  `total_tickets` int(11) DEFAULT 0,
  `avg_rating` decimal(3,2) DEFAULT 0.00,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服表';

-- 文件记录表
CREATE TABLE IF NOT EXISTS `im_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `file_id` varchar(64) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_size` bigint(20) DEFAULT 0,
  `file_type` varchar(50) DEFAULT NULL,
  `mime_type` varchar(100) DEFAULT NULL,
  `storage_type` varchar(20) DEFAULT 'minio',
  `bucket` varchar(100) DEFAULT NULL,
  `object_key` varchar(500) DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  `width` int(11) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `status` tinyint(4) DEFAULT 1,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_id` (`file_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type` (`file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- 插入测试数据
INSERT INTO `im_user` (`username`, `password`, `nickname`, `avatar`, `status`, `online_status`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTm8QVYzN5m5J5qZ5qZ5qZ5qZ5q', '管理员', '/avatar/default.png', 1, 'offline');
