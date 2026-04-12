package com.kama.chatmind.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WebSearchTools implements Tool {

    @Value("${brave.api-key:}")
    private String apiKey;

    private final RestClient restClient = RestClient.create("https://api.search.brave.com");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "webSearchTool";
    }

    @Override
    public String getDescription() {
        return "一个用于搜索互联网的工具，可以通过 Brave Search API 搜索网页信息，返回相关的搜索结果（标题、描述和链接）。";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    /**
     * 搜索互联网
     *
     * @param query 搜索关键词
     * @return 搜索结果（最多返回5条）
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "webSearch",
            description = "搜索互联网获取实时信息。参数为 query（搜索关键词，必填），返回最多5条搜索结果，包括标题、描述和链接。"
    )
    public String webSearch(String query) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "Web search is not available: Brave API key is not configured. Please set 'brave.api-key' in application.yaml.";
        }

        if (query == null || query.trim().isEmpty()) {
            return "错误：搜索关键词不能为空";
        }

        try {
            String responseBody = restClient.get()
                    .uri("/res/v1/web/search?q={query}&count=5", query.trim())
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "gzip")
                    .header("X-Subscription-Token", apiKey)
                    .retrieve()
                    .body(String.class);

            return parseSearchResults(responseBody);
        } catch (Exception e) {
            log.error("Web search failed for query: {}", query, e);
            return "搜索失败：" + e.getMessage();
        }
    }

    private String parseSearchResults(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("web").path("results");

            if (results.isMissingNode() || !results.isArray() || results.isEmpty()) {
                return "未找到相关搜索结果。";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (JsonNode result : results) {
                if (count >= 5) break;
                count++;
                String title = result.path("title").asText("");
                String description = result.path("description").asText("");
                String url = result.path("url").asText("");

                sb.append(count).append(". ").append(title).append("\n");
                if (!description.isEmpty()) {
                    sb.append("   ").append(description).append("\n");
                }
                sb.append("   ").append(url).append("\n\n");
            }

            return sb.toString().trim();
        } catch (Exception e) {
            log.error("Failed to parse search results", e);
            return "搜索结果解析失败：" + e.getMessage();
        }
    }
}
