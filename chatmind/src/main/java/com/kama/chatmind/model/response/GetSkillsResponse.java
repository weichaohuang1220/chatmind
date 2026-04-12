package com.kama.chatmind.model.response;

import com.kama.chatmind.model.vo.SkillVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetSkillsResponse {
    private SkillVO[] skills;
}
