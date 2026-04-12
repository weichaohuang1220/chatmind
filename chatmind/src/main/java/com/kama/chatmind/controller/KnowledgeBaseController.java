package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.model.request.CreateKnowledgeBaseRequest;
import com.kama.chatmind.model.request.UpdateKnowledgeBaseRequest;
import com.kama.chatmind.model.response.CreateKnowledgeBaseResponse;
import com.kama.chatmind.model.response.GetKnowledgeBasesResponse;
import com.kama.chatmind.service.KnowledgeBaseFacadeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseFacadeService knowledgeBaseFacadeService;

    @GetMapping("/knowledge-bases")
    public ApiResponse<GetKnowledgeBasesResponse> getKnowledgeBases() {
        log.info("[KnowledgeBaseController] getKnowledgeBases called");
        try {
            GetKnowledgeBasesResponse response = knowledgeBaseFacadeService.getKnowledgeBases();
            log.info("[KnowledgeBaseController] getKnowledgeBases success, count: {}", response.getKnowledgeBases() != null ? response.getKnowledgeBases().length : 0);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[KnowledgeBaseController] getKnowledgeBases failed", e);
            throw e;
        }
    }

    @PostMapping("/knowledge-bases")
    public ApiResponse<CreateKnowledgeBaseResponse> createKnowledgeBase(@RequestBody CreateKnowledgeBaseRequest request) {
        log.info("[KnowledgeBaseController] createKnowledgeBase called, name: {}", request.getName());
        try {
            CreateKnowledgeBaseResponse response = knowledgeBaseFacadeService.createKnowledgeBase(request);
            log.info("[KnowledgeBaseController] createKnowledgeBase success, id: {}", response.getKnowledgeBaseId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[KnowledgeBaseController] createKnowledgeBase failed", e);
            throw e;
        }
    }

    @DeleteMapping("/knowledge-bases/{knowledgeBaseId}")
    public ApiResponse<Void> deleteKnowledgeBase(@PathVariable String knowledgeBaseId) {
        log.info("[KnowledgeBaseController] deleteKnowledgeBase called, id: {}", knowledgeBaseId);
        try {
            knowledgeBaseFacadeService.deleteKnowledgeBase(knowledgeBaseId);
            log.info("[KnowledgeBaseController] deleteKnowledgeBase success, id: {}", knowledgeBaseId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[KnowledgeBaseController] deleteKnowledgeBase failed, id: {}", knowledgeBaseId, e);
            throw e;
        }
    }

    @PatchMapping("/knowledge-bases/{knowledgeBaseId}")
    public ApiResponse<Void> updateKnowledgeBase(@PathVariable String knowledgeBaseId, @RequestBody UpdateKnowledgeBaseRequest request) {
        log.info("[KnowledgeBaseController] updateKnowledgeBase called, id: {}", knowledgeBaseId);
        try {
            knowledgeBaseFacadeService.updateKnowledgeBase(knowledgeBaseId, request);
            log.info("[KnowledgeBaseController] updateKnowledgeBase success, id: {}", knowledgeBaseId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[KnowledgeBaseController] updateKnowledgeBase failed, id: {}", knowledgeBaseId, e);
            throw e;
        }
    }
}
