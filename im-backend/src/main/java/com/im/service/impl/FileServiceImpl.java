package com.im.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.entity.ImFile;
import com.im.exception.BusinessException;
import com.im.mapper.FileMapper;
import com.im.result.Result;
import com.im.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, ImFile> implements FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Override
    public Result<ImFile> uploadFile(Long userId, MultipartFile file) {
        try {
            // 检查文件
            if (file.isEmpty()) {
                throw new BusinessException("文件不能为空");
            }

            // 检查文件大小（最大 100MB）
            if (file.getSize() > 100 * 1024 * 1024) {
                throw new BusinessException("文件大小不能超过 100MB");
            }

            // 生成文件 ID 和对象键
            String fileId = IdUtil.fastSimpleUUID();
            String originalFilename = file.getOriginalFilename();
            String extension = FileUtil.getSuffix(originalFilename);
            String objectKey = String.format("%s/%s/%s.%s", 
                    LocalDateTime.now().toString().replace("-", "").substring(0, 6),
                    userId,
                    fileId,
                    extension);

            // 确保 bucket 存在
            createBucketIfNotExists();

            // 上传文件到 MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // 生成访问 URL
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(7, TimeUnit.DAYS)
                    .build()
            );

            // 创建文件记录
            ImFile imFile = new ImFile();
            imFile.setFileId(fileId);
            imFile.setUserId(userId);
            imFile.setFileName(originalFilename);
            imFile.setFileSize(file.getSize());
            imFile.setFileType(getFileType(file.getContentType()));
            imFile.setMimeType(file.getContentType());
            imFile.setStorageType("minio");
            imFile.setBucket(bucketName);
            imFile.setObjectKey(objectKey);
            imFile.setUrl(url);
            imFile.setStatus(1);

            baseMapper.insert(imFile);

            log.info("文件上传成功：fileId={}, userId={}, fileName={}, size={}", 
                    fileId, userId, originalFilename, file.getSize());

            return Result.success(imFile);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public Result<ImFile> getFileInfo(Long fileId) {
        ImFile imFile = baseMapper.selectById(fileId);
        if (imFile == null || imFile.getDeleted() != null && imFile.getDeleted() == 1) {
            throw new BusinessException("文件不存在");
        }
        return Result.success(imFile);
    }

    @Override
    public Result<Void> deleteFile(Long userId, Long fileId) {
        ImFile imFile = baseMapper.selectById(fileId);
        if (imFile == null) {
            throw new BusinessException("文件不存在");
        }

        // 检查权限（只有上传者可以删除）
        if (!imFile.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除该文件");
        }

        try {
            // 从 MinIO 删除
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(imFile.getObjectKey())
                    .build()
            );

            // 更新数据库状态
            imFile.setStatus(0);
            baseMapper.updateById(imFile);

            log.info("文件删除成功：fileId={}, userId={}", fileId, userId);
            return Result.success();

        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result<java.util.List<ImFile>> getFileList(Long userId, Integer pageNum, Integer pageSize) {
        // TODO: 实现分页查询
        return Result.success(java.util.Collections.emptyList());
    }

    /**
     * 创建 bucket（如果不存在）
     */
    private void createBucketIfNotExists() throws Exception {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(bucketName)
                .build()
        );

        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            // 设置 bucket 策略为公开读取
            String policyJson = String.format(
                "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                bucketName
            );
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policyJson)
                    .build()
            );

            log.info("创建 bucket 成功：{}", bucketName);
        }
    }

    /**
     * 根据 MIME 类型获取文件类型
     */
    private String getFileType(String mimeType) {
        if (mimeType == null) {
            return "file";
        }
        if (mimeType.startsWith("image/")) {
            return "image";
        } else if (mimeType.startsWith("video/")) {
            return "video";
        } else if (mimeType.startsWith("audio/")) {
            return "audio";
        } else {
            return "file";
        }
    }
}
