package com.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.entity.CallRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通话记录 Mapper
 * 
 * @author IM Team
 * @date 2026-03-14
 */
@Mapper
public interface CallRecordMapper extends BaseMapper<CallRecord> {

    /**
     * 获取通话历史
     */
    List<CallRecord> getCallHistory(@Param("userId") Long userId, 
                                    @Param("offset") int offset, 
                                    @Param("limit") int limit);

    /**
     * 获取未接来电数
     */
    int getMissedCallCount(@Param("userId") Long userId);
}
