package com.maviniciusdev.clara.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessingService {

    private final RAGService ragService;

    public String processMessage(String phoneNumber, String messageBody, String profileName) {

        log.debug("Processando mensagem de {} ({}): {}",
                phoneNumber, profileName, messageBody);

        String normalizedMessage = messageBody.toLowerCase().trim();

        // ============================================
        // 1. SAUDAÇÃO INICIAL
        // ============================================
        if (normalizedMessage.matches(".*(oi|olá|ola|hey|opa|bom dia|boa tarde|boa noite).*") &&
                normalizedMessage.split(" ").length <= 3) {

            String greeting = getTimeBasedGreeting();
            String firstName = getFirstName(profileName);

            return String.format(
                    "%s%s! Tudo bem?\n\n" +
                            "Meu nome é Clara, trabalho aqui no SESI. Em que posso te ajudar?",
                    greeting,
                    firstName != null ? " " + firstName : ""
            );
        }

        // ============================================
        // 2. FALAR COM ATENDENTE HUMANO
        // ============================================
        if (normalizedMessage.matches(".*(atendente|pessoa|humano|alguém|transferir|falar com alguém).*")) {
            return "Claro! Vou te conectar com um atendente. 👤\n\n" +
                    "Você pode ligar agora:\n" +
                    "📞 (71) 3255-6500\n\n" +
                    "Ou enviar um email:\n" +
                    "📧 atendimento@sesibahia.com.br\n\n" +
                    "Informe que estava conversando com a Clara. Eles já vão te ajudar! 😊";
        }

        // ============================================
        // 3. DIRECIONAMENTO PARA DEPARTAMENTOS
        // ============================================

        // 3.1 FINANCEIRO
        if (normalizedMessage.matches(".*(financeiro|pagamento|fatura|boleto|cobrança|pagar|preço|valor|custo).*")) {
            return "Vou te direcionar para o financeiro! 💰\n\n" +
                    "📞 (71) 3255-6500 - Ramal 123\n" +
                    "📧 financeiro@sesibahia.com.br\n\n" +
                    "Horário: Segunda a sexta, 8h às 17h\n\n" +
                    "Posso ajudar em mais alguma coisa?";
        }

        // 3.2 EQUIPE TÉCNICA (Visitas, PGR, PCMSO)
        if (normalizedMessage.matches(".*(tecnic|visita|avaliação|pgr|pcmso|documento|laudo).*")) {
            return "Vou te conectar com a equipe técnica! 🔧\n\n" +
                    "📞 (71) 3255-6500 - Ramal 456\n" +
                    "📧 tecnico@sesibahia.com.br\n\n" +
                    "Eles vão te ajudar com:\n" +
                    "• Agendamento de visitas técnicas\n" +
                    "• PGR e PCMSO\n" +
                    "• Documentos e laudos\n\n" +
                    "Tem mais alguma dúvida?";
        }

        // 3.3 AGENDAMENTO DE EXAMES
        if (normalizedMessage.matches(".*(exame|consulta|agendar|marcar|horário|vaga).*")) {
            return "Para agendamento de exames e consultas! 🏥\n\n" +
                    "📞 (71) 3255-6500 - Ramal 789\n" +
                    "📧 agendamento@sesibahia.com.br\n\n" +
                    "Horário: Segunda a sexta, 7h às 18h\n" +
                    "Sábado: 7h às 12h\n\n" +
                    "Você também pode agendar pelo site:\n" +
                    "🌐 www.sesibahia.com.br/agendar\n\n" +
                    "Precisa de mais informações?";
        }

        // 3.4 RH / CADASTRO
        if (normalizedMessage.matches(".*(cadastro|cadastrar|registro|contratar|contrato).*")) {
            return "Vou te direcionar para o setor de cadastro! 📋\n\n" +
                    "📞 (71) 3255-6500 - Ramal 234\n" +
                    "📧 cadastro@sesibahia.com.br\n\n" +
                    "Eles vão te ajudar com:\n" +
                    "• Cadastro de empresa\n" +
                    "• Contratos\n" +
                    "• Documentação\n\n" +
                    "Posso esclarecer alguma dúvida antes?";
        }

        // ============================================
        // 4. MENU DE OPÇÕES
        // ============================================
        if (normalizedMessage.matches(".*(menu|opções|ajuda|comandos|o que você faz).*")) {
            return "Posso te ajudar com:\n\n" +
                    "📋 Dúvidas sobre PGR e PCMSO\n" +
                    "📄 Documentação necessária\n" +
                    "⏱️ Prazos e processos\n" +
                    "📞 Contato com departamentos\n" +
                    "🗓️ Agendamento de exames\n" +
                    "📧 Enviar documentos\n\n" +
                    "É só perguntar! Estou aqui pra ajudar. 😊";
        }

        // ============================================
        // 5. AGRADECIMENTO
        // ============================================
        if (normalizedMessage.matches(".*(obrigad|valeu|vlw|muito obrigado|agradeço).*")) {
            return "Imagina! Fico feliz em ajudar 😊\n\nQualquer outra dúvida, pode chamar!";
        }

        // ============================================
        // 6. DESPEDIDA
        // ============================================
        if (normalizedMessage.matches(".*(tchau|até logo|até mais|bye|flw|adeus).*")) {
            return "Até mais! Qualquer coisa, é só chamar. Bom dia pra você! 👋";
        }

        // ============================================
        // 7. CONFIRMA QUE ENTENDEU
        // ============================================
        if (normalizedMessage.matches(".*(ok|entendi|certo|beleza|show|tá bom).*") &&
                normalizedMessage.split(" ").length <= 2) {
            return "Ótimo! Tem mais alguma dúvida que eu possa esclarecer?";
        }

        // ============================================
        // 8. USAR RAG PARA DÚVIDAS ESPECÍFICAS
        // ============================================
        return ragService.answerWithContext(messageBody);
    }

    private String getTimeBasedGreeting() {
        LocalTime now = LocalTime.now();

        if (now.isBefore(LocalTime.NOON)) {
            return "Bom dia";
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            return "Boa tarde";
        } else {
            return "Boa noite";
        }
    }

    private String getFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }

        String[] parts = fullName.trim().split(" ");
        return parts[0];
    }
}
