package com.kama.chatmind.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {
    /**
     * 评估的问题
     */
    private String question;

    /**
     * 是否命中（检索到的内容中包含期望的关键词）
     */
    private boolean hit;

    /**
     * 命中的排名分数（用于计算 MRR）
     */
    private double score;

    /**
     * 检索到的文本块列表
     */
    private List<String> retrievedChunks;

    /**
     * LLM-as-Judge 评分（0-1），null 表示未使用 LLM 评估
     */
    private Double llmJudgeScore;

    /**
     * LLM-as-Judge 评分理由
     */
    private String llmJudgeReason;
}
