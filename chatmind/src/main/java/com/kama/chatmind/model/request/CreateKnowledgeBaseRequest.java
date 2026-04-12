package com.kama.chatmind.model.request;

import lombok.Data;

@Data
public class CreateKnowledgeBaseRequest {
    private String name;
    private String description;
}

