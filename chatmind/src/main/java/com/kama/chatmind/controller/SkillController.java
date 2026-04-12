package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.model.request.CreateSkillRequest;
import com.kama.chatmind.model.request.UpdateSkillRequest;
import com.kama.chatmind.model.response.CreateSkillResponse;
import com.kama.chatmind.model.response.GetSkillsResponse;
import com.kama.chatmind.model.vo.SkillVO;
import com.kama.chatmind.service.SkillFacadeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SkillController {

    private final SkillFacadeService skillFacadeService;

    @GetMapping("/skills")
    public ApiResponse<GetSkillsResponse> getSkills() {
        log.info("[SkillController] getSkills called");
        try {
            GetSkillsResponse response = skillFacadeService.getSkills();
            log.info("[SkillController] getSkills success");
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[SkillController] getSkills failed", e);
            throw e;
        }
    }

    @GetMapping("/skills/{skillId}")
    public ApiResponse<SkillVO> getSkillById(@PathVariable String skillId) {
        log.info("[SkillController] getSkillById called, id: {}", skillId);
        try {
            SkillVO response = skillFacadeService.getSkillById(skillId);
            log.info("[SkillController] getSkillById success, id: {}", skillId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[SkillController] getSkillById failed, id: {}", skillId, e);
            throw e;
        }
    }

    @PostMapping("/skills")
    public ApiResponse<CreateSkillResponse> createSkill(@RequestBody CreateSkillRequest request) {
        log.info("[SkillController] createSkill called, name: {}", request.getName());
        try {
            CreateSkillResponse response = skillFacadeService.createSkill(request);
            log.info("[SkillController] createSkill success, id: {}", response.getSkillId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[SkillController] createSkill failed", e);
            throw e;
        }
    }

    @PatchMapping("/skills/{skillId}")
    public ApiResponse<Void> updateSkill(@PathVariable String skillId, @RequestBody UpdateSkillRequest request) {
        log.info("[SkillController] updateSkill called, id: {}", skillId);
        try {
            skillFacadeService.updateSkill(skillId, request);
            log.info("[SkillController] updateSkill success, id: {}", skillId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[SkillController] updateSkill failed, id: {}", skillId, e);
            throw e;
        }
    }

    @DeleteMapping("/skills/{skillId}")
    public ApiResponse<Void> deleteSkill(@PathVariable String skillId) {
        log.info("[SkillController] deleteSkill called, id: {}", skillId);
        try {
            skillFacadeService.deleteSkill(skillId);
            log.info("[SkillController] deleteSkill success, id: {}", skillId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[SkillController] deleteSkill failed, id: {}", skillId, e);
            throw e;
        }
    }
}
