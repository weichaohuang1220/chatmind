package com.kama.chatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kama.chatmind.converter.SkillConverter;
import com.kama.chatmind.exception.BizException;
import com.kama.chatmind.mapper.SkillMapper;
import com.kama.chatmind.model.dto.SkillDTO;
import com.kama.chatmind.model.entity.Skill;
import com.kama.chatmind.model.request.CreateSkillRequest;
import com.kama.chatmind.model.request.UpdateSkillRequest;
import com.kama.chatmind.model.response.CreateSkillResponse;
import com.kama.chatmind.model.response.GetSkillsResponse;
import com.kama.chatmind.model.vo.SkillVO;
import com.kama.chatmind.service.SkillFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SkillFacadeServiceImpl implements SkillFacadeService {

    private final SkillMapper skillMapper;
    private final SkillConverter skillConverter;

    @Override
    public GetSkillsResponse getSkills() {
        List<Skill> skills = skillMapper.selectAll();
        if (skills == null) {
            skills = List.of();
        }
        List<SkillVO> result = new ArrayList<>();
        for (Skill skill : skills) {
            try {
                SkillVO vo = skillConverter.toVO(skill);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetSkillsResponse.builder()
                .skills(result.toArray(new SkillVO[0]))
                .build();
    }

    @Override
    public SkillVO getSkillById(String skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BizException("Skill 不存在: " + skillId);
        }
        try {
            return skillConverter.toVO(skill);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SkillVO> getSkillsByIds(List<String> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return List.of();
        }
        List<Skill> skills = skillMapper.selectByIds(skillIds);
        if (skills == null) {
            skills = List.of();
        }
        List<SkillVO> result = new ArrayList<>();
        for (Skill skill : skills) {
            try {
                SkillVO vo = skillConverter.toVO(skill);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public CreateSkillResponse createSkill(CreateSkillRequest request) {
        try {
            SkillDTO skillDTO = skillConverter.toDTO(request);
            Skill skill = skillConverter.toEntity(skillDTO);

            LocalDateTime now = LocalDateTime.now();
            skill.setCreatedAt(now);
            skill.setUpdatedAt(now);

            int result = skillMapper.insert(skill);
            if (result <= 0) {
                throw new BizException("创建 skill 失败");
            }

            return CreateSkillResponse.builder()
                    .skillId(skill.getId())
                    .build();
        } catch (JsonProcessingException e) {
            throw new BizException("创建 skill 时发生序列化错误: " + e.getMessage());
        }
    }

    @Override
    public void deleteSkill(String skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BizException("Skill 不存在: " + skillId);
        }

        int result = skillMapper.deleteById(skillId);
        if (result <= 0) {
            throw new BizException("删除 skill 失败");
        }
    }

    @Override
    public void updateSkill(String skillId, UpdateSkillRequest request) {
        try {
            Skill existingSkill = skillMapper.selectById(skillId);
            if (existingSkill == null) {
                throw new BizException("Skill 不存在: " + skillId);
            }

            SkillDTO skillDTO = skillConverter.toDTO(existingSkill);
            skillConverter.updateDTOFromRequest(skillDTO, request);
            Skill updatedSkill = skillConverter.toEntity(skillDTO);

            updatedSkill.setId(existingSkill.getId());
            updatedSkill.setCreatedAt(existingSkill.getCreatedAt());
            updatedSkill.setUpdatedAt(LocalDateTime.now());

            int result = skillMapper.updateById(updatedSkill);
            if (result <= 0) {
                throw new BizException("更新 skill 失败");
            }
        } catch (JsonProcessingException e) {
            throw new BizException("更新 skill 时发生序列化错误: " + e.getMessage());
        }
    }
}
