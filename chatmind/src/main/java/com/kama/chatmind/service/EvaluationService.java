package com.kama.chatmind.service;

import com.kama.chatmind.model.dto.EvaluationReport;
import com.kama.chatmind.model.dto.EvaluationResult;

import java.util.List;

public interface EvaluationService {

    /**
     * 评估单个问题的检索效果
     *
     * @param kbId           知识库 ID
     * @param question       查询问题
     * @param expectedChunks 期望检索到的关键词列表
     * @return 评估结果
     */
    EvaluationResult evaluateRetrieval(String kbId, String question, List<String> expectedChunks);

    /**
     * 运行完整评估：读取 qa-dataset.json 中的所有问题，逐一评估并汇总报告
     *
     * @param kbId 知识库 ID
     * @return 评估报告（含 Recall@3 和 MRR）
     */
    EvaluationReport runFullEvaluation(String kbId);

    /**
     * 运行完整评估（含 LLM-as-Judge）：在基础检索评估之上，使用 LLM 对检索结果进行语义评分
     *
     * @param kbId 知识库 ID
     * @return 评估报告（含 Recall@3、MRR 和 LLM-as-Judge 平均分）
     */
    EvaluationReport runFullEvaluationWithJudge(String kbId);
}
