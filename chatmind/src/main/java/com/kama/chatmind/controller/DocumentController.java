package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.model.request.CreateDocumentRequest;
import com.kama.chatmind.model.request.UpdateDocumentRequest;
import com.kama.chatmind.model.response.CreateDocumentResponse;
import com.kama.chatmind.model.response.GetDocumentsResponse;
import com.kama.chatmind.service.DocumentFacadeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DocumentController {

    private final DocumentFacadeService documentFacadeService;

    // 查询所有文档
    @GetMapping("/documents")
    public ApiResponse<GetDocumentsResponse> getDocuments() {
        log.info("[DocumentController] getDocuments called");
        try {
            GetDocumentsResponse response = documentFacadeService.getDocuments();
            log.info("[DocumentController] getDocuments success, count: {}",
                    response.getDocuments() != null ? response.getDocuments().length : 0);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[DocumentController] getDocuments failed", e);
            throw e;
        }
    }

    // 根据 kbId 查询文档
    @GetMapping("/documents/kb/{kbId}")
    public ApiResponse<GetDocumentsResponse> getDocumentsByKbId(@PathVariable String kbId) {
        log.info("[DocumentController] getDocumentsByKbId called, kbId: {}", kbId);
        try {
            GetDocumentsResponse response = documentFacadeService.getDocumentsByKbId(kbId);
            log.info("[DocumentController] getDocumentsByKbId success, kbId: {}, count: {}",
                    kbId, response.getDocuments() != null ? response.getDocuments().length : 0);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[DocumentController] getDocumentsByKbId failed, kbId: {}", kbId, e);
            throw e;
        }
    }

    // 创建文档（仅创建记录，不上传文件）
    @PostMapping("/documents")
    public ApiResponse<CreateDocumentResponse> createDocument(@RequestBody CreateDocumentRequest request) {
        log.info("[DocumentController] createDocument called, request: {}", request);
        try {
            CreateDocumentResponse response = documentFacadeService.createDocument(request);
            log.info("[DocumentController] createDocument success, documentId: {}", response.getDocumentId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[DocumentController] createDocument failed", e);
            throw e;
        }
    }

    // 上传文档（上传文件并创建记录）
    @PostMapping("/documents/upload")
    public ApiResponse<CreateDocumentResponse> uploadDocument(
            @RequestParam("kbId") String kbId,
            @RequestParam("file") MultipartFile file) {
        log.info("[DocumentController] uploadDocument called, kbId: {}, fileName: {}, fileSize: {} bytes",
                kbId, file.getOriginalFilename(), file.getSize());
        try {
            CreateDocumentResponse response = documentFacadeService.uploadDocument(kbId, file);
            log.info("[DocumentController] uploadDocument success, kbId: {}, documentId: {}", kbId, response.getDocumentId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[DocumentController] uploadDocument failed, kbId: {}, fileName: {}", kbId, file.getOriginalFilename(), e);
            throw e;
        }
    }

    // 重新处理文档（重新生成 chunks 和 embeddings）
    @PostMapping("/documents/{documentId}/reprocess")
    public ApiResponse<Void> reprocessDocument(@PathVariable String documentId) {
        log.info("[DocumentController] reprocessDocument called, documentId: {}", documentId);
        try {
            documentFacadeService.reprocessDocument(documentId);
            log.info("[DocumentController] reprocessDocument success, documentId: {}", documentId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[DocumentController] reprocessDocument failed, documentId: {}", documentId, e);
            throw e;
        }
    }

    // 删除文档
    @DeleteMapping("/documents/{documentId}")
    public ApiResponse<Void> deleteDocument(@PathVariable String documentId) {
        log.info("[DocumentController] deleteDocument called, documentId: {}", documentId);
        try {
            documentFacadeService.deleteDocument(documentId);
            log.info("[DocumentController] deleteDocument success, documentId: {}", documentId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[DocumentController] deleteDocument failed, documentId: {}", documentId, e);
            throw e;
        }
    }

    // 更新文档
    @PatchMapping("/documents/{documentId}")
    public ApiResponse<Void> updateDocument(@PathVariable String documentId, @RequestBody UpdateDocumentRequest request) {
        log.info("[DocumentController] updateDocument called, documentId: {}", documentId);
        try {
            documentFacadeService.updateDocument(documentId, request);
            log.info("[DocumentController] updateDocument success, documentId: {}", documentId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[DocumentController] updateDocument failed, documentId: {}", documentId, e);
            throw e;
        }
    }
}
