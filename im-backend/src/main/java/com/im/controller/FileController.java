package com.im.controller;

import com.im.entity.ImFile;
import com.im.result.Result;
import com.im.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件控制器
 */
@Api(tags = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<ImFile> uploadFile(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(userId, file);
    }

    @GetMapping("/info")
    @ApiOperation("获取文件信息")
    public Result<ImFile> getFileInfo(@RequestParam Long fileId) {
        return fileService.getFileInfo(fileId);
    }

    @PostMapping("/delete")
    @ApiOperation("删除文件")
    public Result<Void> deleteFile(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long fileId) {
        return fileService.deleteFile(userId, fileId);
    }

    @GetMapping("/list")
    @ApiOperation("获取文件列表")
    public Result<java.util.List<ImFile>> getFileList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return fileService.getFileList(userId, pageNum, pageSize);
    }
}
