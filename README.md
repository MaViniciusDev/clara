# Clara - SESI Chatbot

Clara é um chatbot inteligente desenvolvido para o SESI, utilizando RAG (Retrieval-Augmented Generation) com integração ao WhatsApp via Twilio.

## 🚀 Tecnologias

- **Java 17** com Spring Boot
- **PostgreSQL** com extensão PGVector para busca vetorial
- **Ollama** para embeddings e chat (modelo llama3.2)
- **Twilio** para integração com WhatsApp
- **Spring AI** para orquestração de IA

## 📋 Pré-requisitos

- Java 17+
- Maven
- PostgreSQL com extensão PGVector
- Ollama instalado localmente
- Conta Twilio (para WhatsApp)

## ⚙️ Configuração

### 1. Clone o repositório

```bash
git clone https://github.com/MaViniciusDev/clara.git
cd clara
```

### 2. Configure o banco de dados

Crie um banco de dados PostgreSQL e habilite a extensão PGVector:

```sql
CREATE DATABASE sesi_chatbot;
\c sesi_chatbot
CREATE EXTENSION vector;
```

### 3. Configure as variáveis de ambiente

Copie o arquivo de exemplo e preencha com suas credenciais:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edite o arquivo `application.properties` e configure:

- **Database**: Suas credenciais do PostgreSQL
- **Twilio**: Account SID, Auth Token e número do WhatsApp
- **Email**: Credenciais SMTP para envio de documentos

### 4. Instale e configure o Ollama

```bash
# Instale o Ollama (https://ollama.ai)
# Baixe os modelos necessários:
ollama pull llama3.2:latest
ollama pull nomic-embed-text
```

### 5. Execute a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:5000`

## 📁 Estrutura do Projeto

```
clara/
├── src/main/java/com/maviniciusdev/clara/
│   ├── config/          # Configurações (Twilio, etc)
│   ├── controller/      # Endpoints REST
│   ├── dto/            # Data Transfer Objects
│   ├── service/        # Lógica de negócio
│   └── ClaraApplication.java
├── src/main/resources/
│   ├── documentos-sesi/  # Documentos para indexação
│   └── application.properties.example
└── pom.xml
```

## 🔒 Segurança

⚠️ **IMPORTANTE**: Nunca commite o arquivo `application.properties` com credenciais reais!

O arquivo está no `.gitignore` e você deve:
1. Copiar o `application.properties.example`
2. Renomear para `application.properties`
3. Preencher com suas credenciais locais

## 📱 Integração com WhatsApp

Configure o webhook do Twilio para apontar para:
```
https://seu-dominio.com/webhook/twilio
```

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT.

## 👨‍💻 Autor

Marcus Vinicius - [@MaViniciusDev](https://github.com/MaViniciusDev)

