package com.maviniciusdev.clara.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    private static final String SYSTEM_PROMPT = """
        Você é Clara, uma profissional do SESI que ajuda clientes com dúvidas sobre 
        saúde e segurança do trabalho.
        
        PERSONALIDADE:
        - Atenciosa e calorosa, mas profissional
        - Conversa de forma natural, como se fosse uma colega
        - Explica as coisas de forma simples, sem complicar
        - Tem paciência para explicar quantas vezes for preciso
        
        COMO RESPONDER:
        - Não use listas ou bullet points, escreva de forma fluída
        - Fale de forma direta e objetiva, como numa conversa real
        - Use "a gente", "nós aqui do SESI", "eu", "você"
        - Máximo 2-3 frases por parágrafo
        - Evite jargões técnicos demais, simplifique
        - Pode usar 1 emoji por resposta, mas só se fizer sentido
        
        O QUE FAZER:
        - Use APENAS informações dos documentos fornecidos
        - Se não souber, diga: "Deixa eu verificar isso com o pessoal aqui e te retorno, ok?"
        - Seja honesta sobre prazos e processos
        - Ofereça ajuda adicional: "Quer que eu explique melhor?" ou "Tem mais alguma dúvida?"
        
        O QUE EVITAR:
        - Não liste tópicos ou serviços sem que perguntem
        - Não seja formal demais
        - Não use palavras como "assistente virtual", "sistema", "IA"
        - Não comece frases com "Com base em..." ou "De acordo com..."
        - Não escreva parágrafos muito longos
        """;


    private static final String USER_PROMPT_TEMPLATE = """
            Com base nos documentos do SESI abaixo, responda a pergunta do cliente:
            
            DOCUMENTOS:
            {context}
            
            PERGUNTA DO CLIENTE:
            {question}
            
            Responda de forma clara, objetiva e amigável, como se estivesse conversando 
            pessoalmente com o cliente. Mantenha o tom profissional mas acessível.
            """;

    public String answerWithContext(String question) {
        try {
            // 1. Buscar documentos relevantes
            List<Document> relevantDocs = vectorStore.similaritySearch(
                    SearchRequest.query(question)
                            .withTopK(4)
                            .withSimilarityThreshold(0.6)
            );

            if (relevantDocs.isEmpty()) {
                log.warn("Nenhum documento relevante encontrado para: {}", question);
                return "Hmm, não encontrei essa informação específica na nossa base de documentos. " +
                        "Você pode reformular a pergunta ou perguntar sobre outro tema? " +
                        "Estou aqui para ajudar com PGR, PCMSO, prazos e documentação. 😊";
            }

            // 2. Montar contexto
            String context = relevantDocs.stream()
                    .map(doc -> String.format(
                            "[Documento: %s]\n%s",
                            doc.getMetadata().get("source"),
                            doc.getContent()
                    ))
                    .collect(Collectors.joining("\n\n---\n\n"));

            log.debug("Contexto montado com {} documentos", relevantDocs.size());

            // 3. Criar prompt
            PromptTemplate promptTemplate = new PromptTemplate(USER_PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of(
                    "context", context,
                    "question", question
            ));

            // 4. Chamar LLM
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(prompt.getContents())
                    .call()
                    .content();

            log.info("Resposta gerada com sucesso para: {}", question);
            return response;

        } catch (Exception e) {
            log.error("Erro ao gerar resposta com RAG", e);
            return "Desculpa, tive um problema técnico aqui. Pode tentar perguntar novamente? " +
                    "Se continuar com erro, me avisa que chamo alguém da equipe técnica. 😊";
        }
    }
}
