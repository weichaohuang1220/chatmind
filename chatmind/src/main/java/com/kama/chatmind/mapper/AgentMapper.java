package com.kama.chatmind.mapper;

import com.kama.chatmind.model.entity.Agent;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 针对表【agent】的数据库操作Mapper
 * @createDate 2025-12-02 14:48:44
 * @Entity com.kama.chatmind.model.entity.Agent
 */
@Mapper
public interface AgentMapper {
    int insert(Agent agent);

    Agent selectById(String id);

    List<Agent> selectAll();

    int deleteById(String id);

    int updateById(Agent agent);
}
