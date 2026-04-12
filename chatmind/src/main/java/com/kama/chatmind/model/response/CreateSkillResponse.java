package com.kama.chatmind.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSkillResponse {
    private String skillId;
}
