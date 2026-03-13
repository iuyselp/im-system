package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.entity.ImFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 文件 Mapper
 */
@Mapper
public interface FileMapper extends BaseMapper<ImFile> {

    /**
     * 根据文件 ID 查询
     */
    @Select("SELECT * FROM im_file WHERE file_id = #{fileId} AND deleted = 0")
    ImFile selectByFileId(String fileId);
}
