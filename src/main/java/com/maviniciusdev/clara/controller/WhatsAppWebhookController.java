package com.maviniciusdev.clara.controller;

import com.maviniciusdev.clara.dto.TwilioWebhookRequest;
import com.maviniciusdev.clara.service.MessageProcessingService;
import com.maviniciusdev.clara.service.DocumentHandlerService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final MessageProcessingService messageProcessingService;
    private final DocumentHandlerService documentHandlerService;

    @Value("${twilio.whatsapp.number}")
    private String twilioWhatsAppNumber;

    private static final int MAX_MESSAGE_LENGTH = 1600;
    private static final int DELAY_BETWEEN_MESSAGES_MS = 1000;

    @PostMapping(value = "/whatsapp", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleWhatsAppMessage(TwilioWebhookRequest request) {
        try {
            String from = request.getFrom();
            String body = request.getBody();
            String profileName = request.getProfileName();

            log.info("Mensagem recebida de: {} ({}) - Conteúdo: {}", from, profileName, body);

            // ============================================
            // VERIFICAR SE TEM DOCUMENTO ANEXADO
            // ============================================
            if (request.getNumMedia() != null &&
                    !request.getNumMedia().trim().isEmpty() &&
                    Integer.parseInt(request.getNumMedia()) > 0) {

                log.info("📎 Documento detectado");
                handleMediaMessage(request);
                return ResponseEntity.ok().build();
            }

            // ============================================
            // PROCESSAR MENSAGEM DE TEXTO NORMAL
            // ============================================
            log.info("💬 Mensagem de texto");
            String response = messageProcessingService.processMessage(from, body, profileName);
            sendWhatsAppMessage(from, response);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Erro ao processar webhook do WhatsApp", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Processa mensagens com documentos/mídias anexadas
     */
    private void handleMediaMessage(TwilioWebhookRequest request) {
        try {
            String from = request.getFrom();
            String mediaUrl = request.getMediaUrl0();
            String mediaType = request.getMediaContentType0();
            String profileName = request.getProfileName();

            log.info("📄 Documento recebido de {} ({}): {} [{}]",
                    from, profileName, mediaUrl, mediaType);

            // Processar documento (baixar, validar, enviar email)
            String resultado = documentHandlerService.processDocument(
                    from,
                    profileName,
                    mediaUrl,
                    mediaType
            );

            // Enviar confirmação
            sendWhatsAppMessage(from, resultado);

        } catch (Exception e) {
            log.error("Erro ao processar documento", e);
            sendWhatsAppMessage(request.getFrom(),
                    "Ops, tive um problema ao processar seu documento. 😅\n\n" +
                            "Pode tentar enviar novamente ou ligar no (71) 3255-6500?");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Webhook Clara está funcionando! ✅");
    }

    /**
     * Envia mensagem WhatsApp dividida em partes se necessário
     */
    private void sendWhatsAppMessage(String to, String messageBody) {
        try {
            List<String> messageParts = splitMessageNaturally(messageBody);

            log.info("Enviando resposta em {} parte(s)", messageParts.size());

            for (int i = 0; i < messageParts.size(); i++) {
                String part = messageParts.get(i);

                Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(twilioWhatsAppNumber),
                        part
                ).create();

                log.info("✓ Parte {}/{} enviada ({} caracteres)",
                        i + 1, messageParts.size(), part.length());

                // Delay entre mensagens para parecer mais natural
                if (i < messageParts.size() - 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(DELAY_BETWEEN_MESSAGES_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            log.info("✅ Resposta completa enviada para: {}", to);

        } catch (Exception e) {
            log.error("Erro ao enviar mensagem WhatsApp", e);
        }
    }

    /**
     * Divide mensagem longa de forma natural, respeitando parágrafos e frases
     */
    private List<String> splitMessageNaturally(String message) {
        List<String> parts = new ArrayList<>();

        if (message.length() <= MAX_MESSAGE_LENGTH) {
            parts.add(message);
            return parts;
        }

        // Dividir por parágrafos primeiro
        String[] paragraphs = message.split("\n\n");
        StringBuilder currentPart = new StringBuilder();

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i];

            // Se adicionar este parágrafo ultrapassar o limite
            if (currentPart.length() + paragraph.length() + 2 > MAX_MESSAGE_LENGTH) {

                // Se o currentPart não está vazio, salva
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString().trim());
                    currentPart = new StringBuilder();
                }

                // Se o parágrafo sozinho é maior que o limite, dividir por frases
                if (paragraph.length() > MAX_MESSAGE_LENGTH) {
                    parts.addAll(splitLongParagraph(paragraph));
                } else {
                    currentPart.append(paragraph);

                    // Adicionar quebra de linha se não for o último
                    if (i < paragraphs.length - 1) {
                        currentPart.append("\n\n");
                    }
                }
            } else {
                // Adicionar parágrafo ao currentPart
                if (currentPart.length() > 0) {
                    currentPart.append("\n\n");
                }
                currentPart.append(paragraph);
            }
        }

        // Adicionar última parte se houver
        if (currentPart.length() > 0) {
            parts.add(currentPart.toString().trim());
        }

        return parts;
    }

    /**
     * Divide parágrafo muito longo por frases
     */
    private List<String> splitLongParagraph(String paragraph) {
        List<String> parts = new ArrayList<>();

        // Dividir por frases (pontos, interrogações, exclamações)
        String[] sentences = paragraph.split("(?<=[.!?])\\s+");
        StringBuilder currentPart = new StringBuilder();

        for (String sentence : sentences) {

            // Se adicionar esta frase ultrapassar o limite
            if (currentPart.length() + sentence.length() + 1 > MAX_MESSAGE_LENGTH) {

                // Salvar parte atual
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString().trim());
                    currentPart = new StringBuilder();
                }

                // Se a frase sozinha é maior que o limite (raro), força quebra
                if (sentence.length() > MAX_MESSAGE_LENGTH) {
                    parts.add(sentence.substring(0, MAX_MESSAGE_LENGTH - 3) + "...");
                    sentence = sentence.substring(MAX_MESSAGE_LENGTH - 3);
                }

                currentPart.append(sentence);
            } else {
                // Adicionar frase
                if (currentPart.length() > 0) {
                    currentPart.append(" ");
                }
                currentPart.append(sentence);
            }
        }

        // Adicionar última parte
        if (currentPart.length() > 0) {
            parts.add(currentPart.toString().trim());
        }

        return parts;
    }
}
