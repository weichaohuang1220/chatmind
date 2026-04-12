package com.kama.chatmind.mapper;

import com.kama.chatmind.model.entity.Skill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description 针对表【skill】的数据库操作Mapper
 * @Entity com.kama.chatmind.model.entity.Skill
 */
@Mapper
public interface SkillMapper {
    int insert(Skill skill);

    Skill selectById(String id);

    List<Skill> selectAll();

    List<Skill> selectByIds(@Param("ids") List<String> ids);

    int deleteById(String id);

    int updateById(Skill skill);
}
