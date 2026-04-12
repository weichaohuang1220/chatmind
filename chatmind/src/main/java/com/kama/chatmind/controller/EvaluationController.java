package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.model.dto.EvaluationReport;
import com.kama.chatmind.service.EvaluationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * 运行完整的 QA 评估，返回 Recall@3 和 MRR 指标
     */
    @PostMapping("/evaluation/run")
    public ApiResponse<EvaluationReport> runEvaluation(@RequestParam String kbId) {
        log.info("[EvaluationController] runEvaluation called, kbId: {}", kbId);
        try {
            EvaluationReport report = evaluationService.runFullEvaluation(kbId);
            log.info("[EvaluationController] runEvaluation success, kbId: {}, recall: {}, mrr: {}",
                    kbId, report.getRecall(), report.getMrr());
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("[EvaluationController] runEvaluation failed, kbId: {}", kbId, e);
            throw e;
        }
    }

    /**
     * 运行完整的 QA 评估（含 LLM-as-Judge），返回 Recall@3、MRR 和 LLM 评分指标
     * 注意：此接口较慢，因为需要对每个问题调用 LLM 进行语义评分
     */
    @PostMapping("/evaluation/run-with-judge")
    public ApiResponse<EvaluationReport> runEvaluationWithJudge(@RequestParam String kbId) {
        log.info("[EvaluationController] runEvaluationWithJudge called, kbId: {}", kbId);
        try {
            EvaluationReport report = evaluationService.runFullEvaluationWithJudge(kbId);
            log.info("[EvaluationController] runEvaluationWithJudge success, kbId: {}, recall: {}, mrr: {}",
                    kbId, report.getRecall(), report.getMrr());
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("[EvaluationController] runEvaluationWithJudge failed, kbId: {}", kbId, e);
            throw e;
        }
    }
}
