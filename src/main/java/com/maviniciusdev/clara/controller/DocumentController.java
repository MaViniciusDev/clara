package com.maviniciusdev.clara.controller;

import com.maviniciusdev.clara.service.DocumentIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIndexingService documentIndexingService;

    @PostMapping("/reindex")
    public ResponseEntity<String> reindexDocuments() {
        documentIndexingService.indexAllDocuments();
        return ResponseEntity.ok("Indexação iniciada! Verifique os logs.");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Serviço de documentos funcionando!");
    }
}
