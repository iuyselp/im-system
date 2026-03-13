package com.im.constant;

/**
 * 系统常量
 */
public class Constants {

    private Constants() {
    }

    // ==================== 用户相关 ====================

    /**
     * 用户状态：正常
     */
    public static final int USER_STATUS_NORMAL = 1;

    /**
     * 用户状态：禁用
     */
    public static final int USER_STATUS_DISABLED = 0;

    /**
     * 在线状态：在线
     */
    public static final String ONLINE_STATUS_ONLINE = "online";

    /**
     * 在线状态：忙碌
     */
    public static final String ONLINE_STATUS_BUSY = "busy";

    /**
     * 在线状态：隐身
     */
    public static final String ONLINE_STATUS_INVISIBLE = "invisible";

    /**
     * 在线状态：离线
     */
    public static final String ONLINE_STATUS_OFFLINE = "offline";

    // ==================== 消息相关 ====================

    /**
     * 消息类型：文本
     */
    public static final String MSG_TYPE_TEXT = "TEXT";

    /**
     * 消息类型：图片
     */
    public static final String MSG_TYPE_IMAGE = "IMAGE";

    /**
     * 消息类型：语音
     */
    public static final String MSG_TYPE_VOICE = "VOICE";

    /**
     * 消息类型：视频
     */
    public static final String MSG_TYPE_VIDEO = "VIDEO";

    /**
     * 消息类型：文件
     */
    public static final String MSG_TYPE_FILE = "FILE";

    /**
     * 消息类型：位置
     */
    public static final String MSG_TYPE_LOCATION = "LOCATION";

    /**
     * 消息类型：系统
     */
    public static final String MSG_TYPE_SYSTEM = "SYSTEM";

    /**
     * 消息类型：通话邀请
     */
    public static final String MSG_TYPE_CALL_INVITE = "CALL_INVITE";

    /**
     * 消息类型：通话应答
     */
    public static final String MSG_TYPE_CALL_ANSWER = "CALL_ANSWER";

    /**
     * 消息类型：通话结束
     */
    public static final String MSG_TYPE_CALL_END = "CALL_END";

    /**
     * 消息状态：正常
     */
    public static final int MSG_STATUS_NORMAL = 1;

    /**
     * 消息状态：撤回
     */
    public static final int MSG_STATUS_REVOKED = 0;

    /**
     * 消息状态：删除
     */
    public static final int MSG_STATUS_DELETED = 2;

    // ==================== 群组相关 ====================

    /**
     * 群成员角色：普通成员
     */
    public static final int GROUP_ROLE_MEMBER = 0;

    /**
     * 群成员角色：管理员
     */
    public static final int GROUP_ROLE_ADMIN = 1;

    /**
     * 群成员角色：群主
     */
    public static final int GROUP_ROLE_OWNER = 2;

    // ==================== 通话相关 ====================

    /**
     * 通话类型：语音
     */
    public static final String CALL_TYPE_AUDIO = "AUDIO";

    /**
     * 通话类型：视频
     */
    public static final String CALL_TYPE_VIDEO = "VIDEO";

    /**
     * 通话状态：已完成
     */
    public static final String CALL_STATUS_COMPLETED = "completed";

    /**
     * 通话状态：未接听
     */
    public static final String CALL_STATUS_MISSED = "missed";

    /**
     * 通话状态：已拒绝
     */
    public static final String CALL_STATUS_REJECTED = "rejected";

    /**
     * 通话状态：已取消
     */
    public static final String CALL_STATUS_CANCELLED = "cancelled";

    // ==================== 客服相关 ====================

    /**
     * 工单状态：开放
     */
    public static final String TICKET_STATUS_OPEN = "open";

    /**
     * 工单状态：已分配
     */
    public static final String TICKET_STATUS_ASSIGNED = "assigned";

    /**
     * 工单状态：待处理
     */
    public static final String TICKET_STATUS_PENDING = "pending";

    /**
     * 工单状态：已解决
     */
    public static final String TICKET_STATUS_RESOLVED = "resolved";

    /**
     * 工单状态：已关闭
     */
    public static final String TICKET_STATUS_CLOSED = "closed";

    // ==================== Redis Key 前缀 ====================

    public static final String REDIS_KEY_PREFIX = "im:";

    public static final String REDIS_KEY_USER_INFO = REDIS_KEY_PREFIX + "user:info:";

    public static final String REDIS_KEY_USER_TOKEN = REDIS_KEY_PREFIX + "user:token:";

    public static final String REDIS_KEY_USER_ONLINE = REDIS_KEY_PREFIX + "user:online:";

    public static final String REDIS_KEY_USER_SESSION = REDIS_KEY_PREFIX + "user:session:";

    public static final String REDIS_KEY_MSG_UNREAD = REDIS_KEY_PREFIX + "msg:unread:";

    public static final String REDIS_KEY_MSG_OFFLINE = REDIS_KEY_PREFIX + "msg:offline:";

    public static final String REDIS_KEY_GROUP_INFO = REDIS_KEY_PREFIX + "group:info:";

    public static final String REDIS_KEY_GROUP_MEMBERS = REDIS_KEY_PREFIX + "group:members:";

    public static final String REDIS_KEY_LOCK = REDIS_KEY_PREFIX + "lock:";
}
