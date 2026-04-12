package com.kama.chatmind.controller;

import com.kama.chatmind.model.dto.EvaluationReport;
import com.kama.chatmind.model.dto.EvaluationResult;
import com.kama.chatmind.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvaluationController.class)
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationService evaluationService;

    private EvaluationReport buildMockReport() {
        EvaluationResult result = EvaluationResult.builder()
                .question("What is ChatMind?")
                .hit(true)
                .score(1.0)
                .retrievedChunks(List.of("chunk1", "chunk2"))
                .build();

        return EvaluationReport.builder()
                .totalQuestions(10)
                .recall(0.85)
                .mrr(0.72)
                .results(List.of(result))
                .build();
    }

    @Test
    void runEvaluation_success() throws Exception {
        EvaluationReport report = buildMockReport();
        when(evaluationService.runFullEvaluation("kb-1")).thenReturn(report);

        mockMvc.perform(post("/api/evaluation/run").param("kbId", "kb-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalQuestions").value(10))
                .andExpect(jsonPath("$.data.recall").value(0.85))
                .andExpect(jsonPath("$.data.mrr").value(0.72))
                .andExpect(jsonPath("$.data.results[0].question").value("What is ChatMind?"));
    }

    @Test
    void runEvaluation_serviceThrows() throws Exception {
        when(evaluationService.runFullEvaluation("bad-kb")).thenThrow(new RuntimeException("KB not found"));

        mockMvc.perform(post("/api/evaluation/run").param("kbId", "bad-kb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void runEvaluationWithJudge_success() throws Exception {
        EvaluationReport report = buildMockReport();
        report.setAverageLlmScore(0.91);
        when(evaluationService.runFullEvaluationWithJudge("kb-1")).thenReturn(report);

        mockMvc.perform(post("/api/evaluation/run-with-judge").param("kbId", "kb-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalQuestions").value(10))
                .andExpect(jsonPath("$.data.averageLlmScore").value(0.91));
    }

    @Test
    void runEvaluationWithJudge_serviceThrows() throws Exception {
        when(evaluationService.runFullEvaluationWithJudge("bad-kb"))
                .thenThrow(new RuntimeException("LLM judge failed"));

        mockMvc.perform(post("/api/evaluation/run-with-judge").param("kbId", "bad-kb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
