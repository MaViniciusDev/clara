package com.maviniciusdev.clara.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentHandlerService {

    private final JavaMailSender mailSender;

    @Value("${documents.upload.path:./uploads}")
    private String uploadPath;

    @Value("${sesi.email.destination:tecnico@sesibahia.com.br}")
    private String destinationEmail;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String processDocument(String phoneNumber, String profileName,
                                  String mediaUrl, String mediaType) {
        try {
            // 1. Validar tipo de arquivo
            if (!isValidFileType(mediaType)) {
                return "Desculpa, esse tipo de arquivo não é suportado. 😕\n\n" +
                        "Aceito apenas:\n" +
                        "📄 PDF, Word, Excel\n" +
                        "📷 Imagens (JPG, PNG)\n\n" +
                        "Pode enviar novamente no formato correto?";
            }

            // 2. Baixar arquivo
            log.info("Baixando arquivo de: {}", mediaUrl);
            File downloadedFile = downloadFileFromTwilio(mediaUrl, mediaType);

            if (downloadedFile == null) {
                throw new Exception("Falha ao baixar arquivo");
            }

            // 3. Enviar email para departamento responsável
            String department = identifyDepartment(mediaType);
            sendEmailWithAttachment(phoneNumber, profileName, downloadedFile, department);

            // 4. Limpar arquivo temporário
            downloadedFile.delete();

            log.info("✅ Documento processado e enviado com sucesso");

            return String.format(
                    "Recebi seu documento! 📄\n\n" +
                            "Encaminhei para: %s\n" +
                            "Eles vão analisar e te retornar em breve.\n\n" +
                            "Precisa enviar mais alguma coisa?",
                    department
            );

        } catch (Exception e) {
            log.error("Erro ao processar documento", e);
            return "Ops, tive um problema ao processar seu documento. 😅\n\n" +
                    "Pode tentar enviar novamente ou ligar no (71) 3255-6500?";
        }
    }

    private boolean isValidFileType(String mediaType) {
        if (mediaType == null) return false;

        return mediaType.contains("pdf") ||
                mediaType.contains("word") ||
                mediaType.contains("document") ||
                mediaType.contains("excel") ||
                mediaType.contains("spreadsheet") ||
                mediaType.contains("image/jpeg") ||
                mediaType.contains("image/jpg") ||
                mediaType.contains("image/png");
    }

    /**
     * Baixa arquivo do Twilio com autenticação HTTP Basic
     */
    private File downloadFileFromTwilio(String mediaUrl, String mediaType) {
        try {
            // Criar diretório de upload se não existir
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Gerar nome único para o arquivo
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            );
            String extension = getFileExtension(mediaType);
            String fileName = "doc_" + timestamp + extension;

            File outputFile = new File(uploadDir, fileName);

            // Configurar conexão HTTP com autenticação
            URL url = new URL(mediaUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // ⭐ AUTENTICAÇÃO HTTP BASIC (TWILIO)
            String auth = twilioAccountSid + ":" + twilioAuthToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 segundos
            connection.setReadTimeout(30000);    // 30 segundos

            // Conectar
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                log.error("Erro ao baixar arquivo do Twilio. Status: {}", responseCode);
                return null;
            }

            // Download
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    totalBytes += bytesRead;

                    // Verificar tamanho máximo
                    if (totalBytes > MAX_FILE_SIZE) {
                        outputFile.delete();
                        throw new Exception("Arquivo muito grande (max 10MB)");
                    }

                    out.write(buffer, 0, bytesRead);
                }
            }

            connection.disconnect();

            log.info("✅ Arquivo baixado: {} ({} bytes)", fileName, outputFile.length());
            return outputFile;

        } catch (Exception e) {
            log.error("❌ Erro ao baixar arquivo do Twilio", e);
            return null;
        }
    }

    private String getFileExtension(String mediaType) {
        if (mediaType == null) return ".bin";

        if (mediaType.contains("pdf")) return ".pdf";
        if (mediaType.contains("word") || mediaType.contains("document")) return ".docx";
        if (mediaType.contains("excel") || mediaType.contains("spreadsheet")) return ".xlsx";
        if (mediaType.contains("jpeg") || mediaType.contains("jpg")) return ".jpg";
        if (mediaType.contains("png")) return ".png";

        return ".bin";
    }

    private String identifyDepartment(String mediaType) {
        if (mediaType == null) return "Documentação";

        if (mediaType.contains("image")) {
            return "Equipe Técnica";
        }
        if (mediaType.contains("pdf") || mediaType.contains("document")) {
            return "Documentação";
        }

        return "Administrativo";
    }

    private void sendEmailWithAttachment(String phoneNumber, String profileName,
                                         File attachment, String department) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@sesibahia.com.br");
            helper.setTo(destinationEmail);
            helper.setSubject("📄 Documento recebido via WhatsApp - " +
                    (profileName != null ? profileName : "Cliente"));

            String emailBody = String.format(
                    "<h3>Documento recebido via WhatsApp Bot Clara</h3>" +
                            "<p><strong>Cliente:</strong> %s</p>" +
                            "<p><strong>Telefone:</strong> %s</p>" +
                            "<p><strong>Departamento:</strong> %s</p>" +
                            "<p><strong>Data:</strong> %s</p>" +
                            "<hr>" +
                            "<p>Documento em anexo.</p>" +
                            "<p style='color: #666; font-size: 12px;'>Enviado automaticamente pelo Clara Bot</p>",
                    profileName != null ? profileName : "Não informado",
                    phoneNumber,
                    department,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );

            helper.setText(emailBody, true);
            helper.addAttachment(attachment.getName(), attachment);

            mailSender.send(message);
            log.info("✉️ Email enviado para: {}", destinationEmail);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email", e);
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }
}
