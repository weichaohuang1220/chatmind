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
public class EvaluationReport {
    /**
     * 总评估问题数
     */
    private int totalQuestions;

    /**
     * Recall@3: 至少检索到一个期望关键词的问题占比
     */
    private double recall;

    /**
     * Mean Reciprocal Rank: 首次命中排名的倒数的均值
     */
    private double mrr;

    /**
     * 每个问题的详细评估结果
     */
    private List<EvaluationResult> results;

    /**
     * LLM-as-Judge 平均评分（0-1），null 表示未使用 LLM 评估
     */
    private Double averageLlmScore;
}
