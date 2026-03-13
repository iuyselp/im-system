package com.im.controller;

import com.im.entity.Group;
import com.im.entity.GroupMember;
import com.im.result.Result;
import com.im.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组控制器
 */
@Api(tags = "群组管理")
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    @ApiOperation("创建群组")
    public Result<Group> createGroup(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群名称") @RequestParam String name,
            @ApiParam("群头像") @RequestParam(required = false) String avatar,
            @ApiParam("成员 ID 列表") @RequestParam(required = false) List<Long> memberIds) {
        return groupService.createGroup(userId, name, avatar, memberIds);
    }

    @GetMapping("/info")
    @ApiOperation("获取群组信息")
    public Result<Group> getGroupInfo(@ApiParam("群 ID") @RequestParam Long groupId) {
        return groupService.getGroupInfo(groupId);
    }

    @PutMapping("/info")
    @ApiOperation("更新群组信息")
    public Result<Group> updateGroupInfo(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId,
            @RequestBody Group group) {
        return groupService.updateGroupInfo(userId, groupId, group);
    }

    @PostMapping("/dismiss")
    @ApiOperation("解散群组")
    public Result<Void> dismissGroup(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId) {
        return groupService.dismissGroup(userId, groupId);
    }

    @PostMapping("/transfer")
    @ApiOperation("转让群主")
    public Result<Void> transferGroup(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId,
            @ApiParam("新群主 ID") @RequestParam Long newOwnerId) {
        return groupService.transferGroup(userId, groupId, newOwnerId);
    }

    @GetMapping("/members")
    @ApiOperation("获取群成员列表")
    public Result<List<GroupMember>> getMemberList(@ApiParam("群 ID") @RequestParam Long groupId) {
        return groupService.getMemberList(groupId);
    }

    @PostMapping("/invite")
    @ApiOperation("邀请成员")
    public Result<Void> inviteMembers(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId,
            @ApiParam("成员 ID 列表") @RequestParam List<Long> memberIds) {
        return groupService.inviteMembers(userId, groupId, memberIds);
    }

    @PostMapping("/remove")
    @ApiOperation("移除成员")
    public Result<Void> removeMembers(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId,
            @ApiParam("成员 ID 列表") @RequestParam List<Long> memberIds) {
        return groupService.removeMembers(userId, groupId, memberIds);
    }

    @PostMapping("/quit")
    @ApiOperation("退出群组")
    public Result<Void> quitGroup(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId) {
        return groupService.quitGroup(userId, groupId);
    }

    @PostMapping("/admin")
    @ApiOperation("设置管理员")
    public Result<Void> setAdmin(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId,
            @ApiParam("成员 ID") @RequestParam Long memberId,
            @ApiParam("是否管理员") @RequestParam boolean isAdmin) {
        return groupService.setAdmin(userId, groupId, memberId, isAdmin);
    }

    @PostMapping("/mute")
    @ApiOperation("禁言成员")
    public Result<Void> muteMember(
            @RequestAttribute("userId") Long userId,
            @ApiParam("群 ID") @RequestParam Long groupId,
            @ApiParam("成员 ID") @RequestParam Long memberId,
            @ApiParam("禁言时长 (分钟)") @RequestParam Long durationMinutes) {
        return groupService.muteMember(userId, groupId, memberId, durationMinutes);
    }

    @GetMapping("/my")
    @ApiOperation("获取我加入的群组")
    public Result<List<Group>> getUserGroups(@RequestAttribute("userId") Long userId) {
        return groupService.getUserGroups(userId);
    }
}
