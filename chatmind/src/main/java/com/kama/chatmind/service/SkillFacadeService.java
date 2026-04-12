package com.kama.chatmind.service;

import com.kama.chatmind.model.request.CreateSkillRequest;
import com.kama.chatmind.model.request.UpdateSkillRequest;
import com.kama.chatmind.model.response.CreateSkillResponse;
import com.kama.chatmind.model.response.GetSkillsResponse;
import com.kama.chatmind.model.vo.SkillVO;

import java.util.List;

public interface SkillFacadeService {
    GetSkillsResponse getSkills();

    SkillVO getSkillById(String skillId);

    List<SkillVO> getSkillsByIds(List<String> skillIds);

    CreateSkillResponse createSkill(CreateSkillRequest request);

    void deleteSkill(String skillId);

    void updateSkill(String skillId, UpdateSkillRequest request);
}
