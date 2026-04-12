package com.kama.chatmind.service.impl;

import com.kama.chatmind.mapper.ChunkBgeM3Mapper;
import com.kama.chatmind.model.entity.ChunkBgeM3;
import com.kama.chatmind.service.RagService;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class RagServiceImpl implements RagService {

    // 封装本地的模型调用
    private final WebClient webClient;
    private final ChunkBgeM3Mapper chunkBgeM3Mapper;

    public RagServiceImpl(ChunkBgeM3Mapper chunkBgeM3Mapper) {
        this.webClient = WebClient.create("http://localhost:11434");
        this.chunkBgeM3Mapper = chunkBgeM3Mapper;
    }

    @Data
    private static class EmbeddingResponse {
        private float[] embedding;
    }

    private float[] doEmbed(String text) {
        EmbeddingResponse resp = webClient.post()
                .uri("/api/embeddings")
                .bodyValue(Map.of(
                        "model", "bge-m3",
                        "prompt", text
                ))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block();
        Assert.notNull(resp, "Embedding response cannot be null");
        return resp.getEmbedding();
    }

    @Override
    public float[] embed(String text) {
        return doEmbed(text);
    }

    @Override
    public List<String> similaritySearch(String kbId, String title) {
        return similaritySearch(kbId, title, 3);
    }

    @Override
    public List<String> similaritySearch(String kbId, String title, int topK) {
        String queryEmbedding = toPgVector(doEmbed(title));
        List<ChunkBgeM3> chunks = chunkBgeM3Mapper.similaritySearch(kbId, queryEmbedding, topK);
        if (chunks == null) {
            return Collections.emptyList();
        }
        return chunks.stream().map(chunk -> {
            String content = chunk.getContent();
            String metadata = chunk.getMetadata();
            // 如果 metadata 中包含 title，则在内容前面加上标题标记，帮助 LLM 理解上下文
            if (metadata != null && metadata.contains("\"title\"")) {
                String sectionTitle = extractTitleFromMetadata(metadata);
                if (sectionTitle != null && !sectionTitle.isEmpty()) {
                    return "【" + sectionTitle + "】" + content;
                }
            }
            return content;
        }).toList();
    }

    /**
     * 从 metadata JSON 字符串中提取 title 字段值
     */
    private String extractTitleFromMetadata(String metadata) {
        try {
            int start = metadata.indexOf("\"title\":\"");
            if (start == -1) return null;
            start += "\"title\":\"".length();
            int end = metadata.indexOf("\"", start);
            if (end == -1) return null;
            return metadata.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String toPgVector(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i]);
            if (i < v.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
