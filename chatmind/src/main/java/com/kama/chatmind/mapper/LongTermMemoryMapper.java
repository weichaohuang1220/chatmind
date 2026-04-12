package com.kama.chatmind.mapper;

import com.kama.chatmind.model.entity.LongTermMemory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 长期记忆 Mapper
 * @Entity com.kama.chatmind.model.entity.LongTermMemory
 */
@Mapper
public interface LongTermMemoryMapper {

    List<LongTermMemory> selectByAgentId(String agentId);

    int insert(LongTermMemory memory);

    int deleteByAgentId(String agentId);
}
