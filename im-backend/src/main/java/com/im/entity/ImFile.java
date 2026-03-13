package com.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件实体
 */
@Data
@Accessors(chain = true)
@TableName("im_file")
public class ImFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件唯一 ID
     */
    private String fileId;

    /**
     * 上传用户 ID
     */
    private Long userId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 文件类型 image/video/audio/file
     */
    private String fileType;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * 存储类型 minio/oss/s3
     */
    private String storageType;

    /**
     * 存储桶
     */
    private String bucket;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 访问 URL
     */
    private String url;

    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;

    /**
     * 宽度 (图片/视频)
     */
    private Integer width;

    /**
     * 高度 (图片/视频)
     */
    private Integer height;

    /**
     * 时长 (音频/视频，秒)
     */
    private Integer duration;

    /**
     * 状态 1-正常 0-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
