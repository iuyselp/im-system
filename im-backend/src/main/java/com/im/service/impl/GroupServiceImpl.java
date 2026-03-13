package com.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.constant.Constants;
import com.im.entity.Group;
import com.im.entity.GroupMember;
import com.im.exception.BusinessException;
import com.im.mapper.GroupMapper;
import com.im.mapper.GroupMemberMapper;
import com.im.result.Result;
import com.im.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 群组服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Group> createGroup(Long userId, String name, String avatar, List<Long> memberIds) {
        // 创建群组
        Group group = new Group();
        group.setName(name);
        group.setAvatar(avatar);
        group.setOwnerId(userId);
        group.setMaxMembers(500);
        group.setMemberCount(1 + (memberIds != null ? memberIds.size() : 0));
        group.setStatus(1);
        group.setJoinType(0);
        group.setChatType(0);

        groupMapper.insert(group);

        // 添加群主为成员
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(group.getId());
        ownerMember.setUserId(userId);
        ownerMember.setRole(Constants.GROUP_ROLE_OWNER);
        ownerMember.setJoinedAt(LocalDateTime.now());
        groupMemberMapper.insert(ownerMember);

        // 添加其他成员
        if (memberIds != null && !memberIds.isEmpty()) {
            List<GroupMember> members = new ArrayList<>();
            for (Long memberId : memberIds) {
                GroupMember member = new GroupMember();
                member.setGroupId(group.getId());
                member.setUserId(memberId);
                member.setRole(Constants.GROUP_ROLE_MEMBER);
                member.setJoinedAt(LocalDateTime.now());
                member.setJoinedBy(userId);
                members.add(member);
            }
            if (!members.isEmpty()) {
                for (GroupMember member : members) {
                    groupMemberMapper.insert(member);
                }
            }
        }

        log.info("创建群组成功：groupId={}, name={}, ownerId={}", group.getId(), name, userId);
        return Result.success(group);
    }

    @Override
    public Result<Group> getGroupInfo(Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null || group.getDeleted() != null && group.getDeleted() == 1) {
            throw new BusinessException("群组不存在");
        }
        return Result.success(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Group> updateGroupInfo(Long userId, Long groupId, Group group) {
        // 检查权限
        GroupMember member = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (member == null || (member.getRole() != Constants.GROUP_ROLE_OWNER && member.getRole() != Constants.GROUP_ROLE_ADMIN)) {
            throw new BusinessException("无权限修改群信息");
        }

        Group existGroup = groupMapper.selectById(groupId);
        if (existGroup == null) {
            throw new BusinessException("群组不存在");
        }

        // 更新允许的字段
        if (group.getName() != null) {
            existGroup.setName(group.getName());
        }
        if (group.getAvatar() != null) {
            existGroup.setAvatar(group.getAvatar());
        }
        if (group.getNotice() != null) {
            existGroup.setNotice(group.getNotice());
            existGroup.setNoticeUpdatedBy(userId);
            existGroup.setNoticeUpdatedAt(LocalDateTime.now());
        }
        if (group.getJoinType() != null) {
            existGroup.setJoinType(group.getJoinType());
        }

        groupMapper.updateById(existGroup);

        return Result.success(existGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> dismissGroup(Long userId, Long groupId) {
        // 检查是否是群主
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        if (!group.getOwnerId().equals(userId)) {
            throw new BusinessException("只有群主可以解散群组");
        }

        // 更新群组状态
        group.setStatus(0);
        groupMapper.updateById(group);

        // 删除群成员
        groupMemberMapper.deleteByGroupId(groupId);

        log.info("解散群组：groupId={}, userId={}", groupId, userId);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> transferGroup(Long userId, Long groupId, Long newOwnerId) {
        // 检查是否是群主
        Group group = groupMapper.selectById(groupId);
        if (group == null || !group.getOwnerId().equals(userId)) {
            throw new BusinessException("只有群主可以转让群组");
        }

        // 检查新群主是否是成员
        GroupMember newOwnerMember = groupMemberMapper.selectByGroupAndUser(groupId, newOwnerId);
        if (newOwnerMember == null) {
            throw new BusinessException("新群主必须是群成员");
        }

        // 更新群主
        group.setOwnerId(newOwnerId);
        groupMapper.updateById(group);

        // 更新原群主角色为成员
        GroupMember oldOwnerMember = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (oldOwnerMember != null) {
            oldOwnerMember.setRole(Constants.GROUP_ROLE_MEMBER);
            groupMemberMapper.updateById(oldOwnerMember);
        }

        // 更新新群主角色为群主
        newOwnerMember.setRole(Constants.GROUP_ROLE_OWNER);
        groupMemberMapper.updateById(newOwnerMember);

        log.info("转让群组：groupId={}, fromId={}, toId={}", groupId, userId, newOwnerId);
        return Result.success();
    }

    @Override
    public Result<List<GroupMember>> getMemberList(Long groupId) {
        List<GroupMember> members = groupMemberMapper.selectByGroupId(groupId);
        return Result.success(members);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> inviteMembers(Long userId, Long groupId, List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Result.success();
        }

        // 检查群组
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        // 检查邀请人权限
        GroupMember inviter = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (inviter == null) {
            throw new BusinessException("不是群成员");
        }

        // 检查群成员数量
        int currentCount = groupMemberMapper.countByGroupId(groupId);
        if (currentCount + memberIds.size() > group.getMaxMembers()) {
            throw new BusinessException("群成员已满");
        }

        // 添加成员
        for (Long memberId : memberIds) {
            // 检查是否已是成员
            GroupMember existMember = groupMemberMapper.selectByGroupAndUser(groupId, memberId);
            if (existMember != null) {
                continue; // 已是成员，跳过
            }

            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(memberId);
            member.setRole(Constants.GROUP_ROLE_MEMBER);
            member.setJoinedAt(LocalDateTime.now());
            member.setJoinedBy(userId);
            groupMemberMapper.insert(member);
        }

        // 更新群成员数
        group.setMemberCount(groupMemberMapper.countByGroupId(groupId));
        groupMapper.updateById(group);

        log.info("邀请成员：groupId={}, inviterId={}, memberIds={}", groupId, userId, memberIds);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeMembers(Long userId, Long groupId, List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Result.success();
        }

        // 检查权限
        GroupMember remover = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (remover == null || (remover.getRole() != Constants.GROUP_ROLE_OWNER && remover.getRole() != Constants.GROUP_ROLE_ADMIN)) {
            throw new BusinessException("无权限移除成员");
        }

        // 不能移除群主
        Group group = groupMapper.selectById(groupId);
        if (memberIds.contains(group.getOwnerId())) {
            throw new BusinessException("不能移除群主");
        }

        for (Long memberId : memberIds) {
            groupMemberMapper.deleteByGroupAndUser(groupId, memberId);
        }

        // 更新群成员数
        group.setMemberCount(groupMemberMapper.countByGroupId(groupId));
        groupMapper.updateById(group);

        log.info("移除成员：groupId={}, operatorId={}, memberIds={}", groupId, userId, memberIds);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> quitGroup(Long userId, Long groupId) {
        GroupMember member = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (member == null) {
            throw new BusinessException("不是群成员");
        }

        // 群主不能退出，只能转让或解散
        if (member.getRole() == Constants.GROUP_ROLE_OWNER) {
            throw new BusinessException("群主不能退出群组，请先转让或解散");
        }

        groupMemberMapper.deleteByGroupAndUser(groupId, userId);

        // 更新群成员数
        Group group = groupMapper.selectById(groupId);
        if (group != null) {
            group.setMemberCount(groupMemberMapper.countByGroupId(groupId));
            groupMapper.updateById(group);
        }

        log.info("退出群组：groupId={}, userId={}", groupId, userId);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> setAdmin(Long userId, Long groupId, Long memberId, boolean isAdmin) {
        // 检查是否是群主
        Group group = groupMapper.selectById(groupId);
        if (group == null || !group.getOwnerId().equals(userId)) {
            throw new BusinessException("只有群主可以设置管理员");
        }

        GroupMember member = groupMemberMapper.selectByGroupAndUser(groupId, memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        member.setRole(isAdmin ? Constants.GROUP_ROLE_ADMIN : Constants.GROUP_ROLE_MEMBER);
        groupMemberMapper.updateById(member);

        log.info("设置管理员：groupId={}, memberId={}, isAdmin={}", groupId, memberId, isAdmin);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> muteMember(Long userId, Long groupId, Long memberId, Long durationMinutes) {
        // 检查权限
        GroupMember operator = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (operator == null || (operator.getRole() != Constants.GROUP_ROLE_OWNER && operator.getRole() != Constants.GROUP_ROLE_ADMIN)) {
            throw new BusinessException("无权限禁言成员");
        }

        GroupMember member = groupMemberMapper.selectByGroupAndUser(groupId, memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        // 不能禁言群主和管理员
        if (member.getRole() == Constants.GROUP_ROLE_OWNER || member.getRole() == Constants.GROUP_ROLE_ADMIN) {
            throw new BusinessException("不能禁言群主和管理员");
        }

        if (durationMinutes > 0) {
            member.setMuteUntil(LocalDateTime.now().plusMinutes(durationMinutes));
        } else {
            member.setMuteUntil(null); // 解除禁言
        }

        groupMemberMapper.updateById(member);

        log.info("禁言成员：groupId={}, operatorId={}, memberId={}, duration={}", groupId, userId, memberId, durationMinutes);
        return Result.success();
    }

    @Override
    public Result<List<Group>> getUserGroups(Long userId) {
        List<Group> groups = groupMapper.selectUserGroups(userId);
        return Result.success(groups);
    }
}
