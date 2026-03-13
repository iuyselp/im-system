package com.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.entity.Group;
import com.im.entity.GroupMember;
import com.im.result.Result;

import java.util.List;

/**
 * 群组服务接口
 */
public interface GroupService extends IService<Group> {

    /**
     * 创建群组
     */
    Result<Group> createGroup(Long userId, String name, String avatar, List<Long> memberIds);

    /**
     * 获取群组信息
     */
    Result<Group> getGroupInfo(Long groupId);

    /**
     * 更新群组信息
     */
    Result<Group> updateGroupInfo(Long userId, Long groupId, Group group);

    /**
     * 解散群组
     */
    Result<Void> dismissGroup(Long userId, Long groupId);

    /**
     * 转让群主
     */
    Result<Void> transferGroup(Long userId, Long groupId, Long newOwnerId);

    /**
     * 获取群成员列表
     */
    Result<List<GroupMember>> getMemberList(Long groupId);

    /**
     * 邀请成员
     */
    Result<Void> inviteMembers(Long userId, Long groupId, List<Long> memberIds);

    /**
     * 移除成员
     */
    Result<Void> removeMembers(Long userId, Long groupId, List<Long> memberIds);

    /**
     * 退出群组
     */
    Result<Void> quitGroup(Long userId, Long groupId);

    /**
     * 设置管理员
     */
    Result<Void> setAdmin(Long userId, Long groupId, Long memberId, boolean isAdmin);

    /**
     * 禁言成员
     */
    Result<Void> muteMember(Long userId, Long groupId, Long memberId, Long durationMinutes);

    /**
     * 获取用户加入的群组列表
     */
    Result<List<Group>> getUserGroups(Long userId);
}
