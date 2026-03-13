package com.im.service;

import com.im.entity.File;
import com.im.result.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     */
    Result<File> uploadFile(Long userId, MultipartFile file);

    /**
     * 获取文件信息
     */
    Result<File> getFileInfo(Long fileId);

    /**
     * 删除文件
     */
    Result<Void> deleteFile(Long userId, Long fileId);

    /**
     * 获取文件列表
     */
    Result<java.util.List<File>> getFileList(Long userId, Integer pageNum, Integer pageSize);
}
