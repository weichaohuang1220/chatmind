package com.kama.chatmind.model.response;

import com.kama.chatmind.model.vo.DocumentVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDocumentsResponse {
    private DocumentVO[] documents;
}

