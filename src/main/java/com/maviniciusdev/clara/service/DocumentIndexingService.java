package com.maviniciusdev.clara.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIndexingService {

    private final VectorStore vectorStore;

    @Value("${documents.sesi.path}")
    private String documentsPath;

    @Value("${documents.sesi.reindex-on-startup}")
    private boolean reindexOnStartup;

    @EventListener(ApplicationReadyEvent.class)
    public void indexDocumentsOnStartup() {
        if (reindexOnStartup) {
            log.info("Iniciando indexação de documentos SESI...");
            indexAllDocuments();
        } else {
            log.info("Indexação automática desabilitada. Use o endpoint /api/documents/reindex");
        }
    }

    public void indexAllDocuments() {
        try {
            List<Document> allDocuments = new ArrayList<>();

            File baseDir = new File(documentsPath);
            if (!baseDir.exists()) {
                log.warn("Diretório de documentos não encontrado: {}", documentsPath);
                return;
            }

            // Buscar todos os arquivos DOCX recursivamente
            List<File> docxFiles = findDocxFiles(baseDir);
            log.info("Encontrados {} arquivos DOCX", docxFiles.size());

            if (docxFiles.isEmpty()) {
                log.warn("Nenhum arquivo DOCX encontrado em: {}", documentsPath);
                return;
            }

            for (File file : docxFiles) {
                try {
                    List<Document> docs = loadDocxFile(file);
                    allDocuments.addAll(docs);
                    log.info("✓ Carregado: {} ({} chunks)", file.getName(), docs.size());
                } catch (Exception e) {
                    log.error("✗ Erro ao carregar arquivo: {}", file.getName(), e);
                }
            }

            if (!allDocuments.isEmpty()) {
                // Adicionar ao vector store
                vectorStore.add(allDocuments);
                log.info("✅ Indexação concluída! {} chunks adicionados ao vector store",
                        allDocuments.size());
            } else {
                log.warn("Nenhum documento foi indexado.");
            }

        } catch (Exception e) {
            log.error("Erro na indexação de documentos", e);
        }
    }

    private List<File> findDocxFiles(File directory) {
        List<File> docxFiles = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    docxFiles.addAll(findDocxFiles(file));
                } else if (isDocxFile(file)) {
                    docxFiles.add(file);
                }
            }
        }

        return docxFiles;
    }

    private boolean isDocxFile(File file) {
        String name = file.getName().toLowerCase();
        return (name.endsWith(".docx") || name.endsWith(".doc"))
                && !name.startsWith("~$");
    }

    private List<Document> loadDocxFile(File file) {
        try {
            // Usar TikaDocumentReader do Spring AI (mais compatível)
            TikaDocumentReader reader = new TikaDocumentReader(
                    new FileSystemResource(file)
            );

            List<Document> documents = reader.get();

            if (documents.isEmpty()) {
                log.warn("Nenhum conteúdo extraído de: {}", file.getName());
                return Collections.emptyList();
            }

            // Adicionar metadados
            String category = detectCategory(file);
            for (Document doc : documents) {
                Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                metadata.put("source", file.getName());
                metadata.put("category", category);
                metadata.put("path", file.getPath());
                metadata.put("indexed_at", LocalDateTime.now().toString());

                // Atualizar metadados do documento
                doc.getMetadata().putAll(metadata);
            }

            // Dividir em chunks menores
            TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 5, 10000, true);
            List<Document> allChunks = new ArrayList<>();

            for (Document doc : documents) {
                allChunks.addAll(splitter.split(doc));
            }

            return allChunks;

        } catch (Exception e) {
            log.error("Erro ao processar arquivo: {}", file.getName(), e);
            return Collections.emptyList();
        }
    }

    private String detectCategory(File file) {
        String path = file.getPath().toLowerCase();

        if (path.contains("pgr")) return "PGR";
        if (path.contains("pcmso")) return "PCMSO";
        if (path.contains("exame")) return "EXAMES";
        if (path.contains("produto")) return "PRODUTOS";
        if (path.contains("processo")) return "PROCESSOS";
        if (path.contains("geral")) return "GERAL";

        String filename = file.getName().toLowerCase();
        if (filename.contains("pgr")) return "PGR";
        if (filename.contains("pcmso")) return "PCMSO";
        if (filename.contains("exame")) return "EXAMES";
        if (filename.contains("m1") || filename.contains("modelo")) return "PROCESSOS";
        if (filename.contains("viva")) return "PRODUTOS";

        return "GERAL";
    }
}
