# 🤖 Clara - Assistente Virtual SESI Saúde

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PGVector-blue?style=for-the-badge&logo=postgresql)
![Ollama](https://img.shields.io/badge/Ollama-llama3.2-purple?style=for-the-badge)

**Desenvolvido para o Hackathon SESI Saúde - Feira de Santana/BA** 🏆

</div>

---

## 📖 Sobre o Projeto

**Clara** é uma assistente virtual inteligente desenvolvida especialmente para o **Hackathon SESI Saúde de Feira de Santana**, com o objetivo de revolucionar o atendimento e suporte aos serviços de saúde ocupacional do SESI.

A solução utiliza tecnologias de ponta em Inteligência Artificial, combinando **RAG (Retrieval-Augmented Generation)** com integração ao **WhatsApp**, permitindo que trabalhadores, gestores e profissionais de saúde obtenham informações precisas e instantâneas sobre:

- 🏥 **PCMSO** (Programa de Controle Médico de Saúde Ocupacional)
- 📋 **PGR** (Programa de Gerenciamento de Riscos)
- 💉 **Exames Ocupacionais** por categoria profissional
- 📱 **Serviços Institucionais** do SESI
- 🎯 **SESI Viva+** e outros produtos
- 📄 **Processos e Fluxos** de atendimento

### 🎯 Problema que Resolvemos

Tradicionalmente, trabalhadores e empresas enfrentam dificuldades para:
- Entender processos complexos de saúde ocupacional
- Encontrar informações específicas sobre exames e programas
- Ter acesso rápido a documentação e orientações
- Agendar e acompanhar serviços de forma eficiente

**Clara** democratiza o acesso à informação através de uma interface conversacional simples e acessível via WhatsApp!

## 🚀 Tecnologias e Arquitetura

### Stack Principal

- **Java 17** com Spring Boot 3.x
- **PostgreSQL** com extensão PGVector para busca vetorial semântica
- **Ollama** (llama3.2) para processamento de linguagem natural
- **Twilio API** para integração com WhatsApp
- **Spring AI** para orquestração de IA e RAG
- **Apache POI** para processamento de documentos

### Arquitetura RAG

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│  WhatsApp   │ ───► │    Twilio    │ ───► │   Clara     │
│   (User)    │      │   Webhook    │      │  (Backend)  │
└─────────────┘      └──────────────┘      └──────┬──────┘
                                                   │
                                                   ▼
                            ┌──────────────────────────────────┐
                            │   RAG Pipeline                   │
                            │  ┌─────────────────────────┐    │
                            │  │ 1. Query Embedding      │    │
                            │  └───────────┬─────────────┘    │
                            │              ▼                   │
                            │  ┌─────────────────────────┐    │
                            │  │ 2. Vector Search        │    │
                            │  │    (PGVector)           │    │
                            │  └───────────┬─────────────┘    │
                            │              ▼                   │
                            │  ┌─────────────────────────┐    │
                            │  │ 3. Context Retrieval    │    │
                            │  └───────────┬─────────────┘    │
                            │              ▼                   │
                            │  ┌─────────────────────────┐    │
                            │  │ 4. LLM Generation       │    │
                            │  │    (Llama 3.2)          │    │
                            │  └─────────────────────────┘    │
                            └──────────────────────────────────┘
```

## ✨ Funcionalidades

### 💬 Conversação Inteligente via WhatsApp
- Respostas contextualizadas baseadas em documentação oficial do SESI
- Compreensão de linguagem natural em português
- Histórico de conversação mantido durante a sessão

### 🔍 Busca Semântica Avançada
- Indexação automática de documentos (DOCX, PDF, TXT)
- Busca vetorial com PGVector para maior precisão
- Recuperação de contexto relevante de múltiplos documentos

### 📚 Base de Conhecimento
Documentos indexados incluem:
- Dúvidas frequentes sobre PCMSO
- Dúvidas frequentes sobre PGR
- Guia completo de preenchimento M1
- Fluxo completo PGR/PCMSO
- Exames por ocupação industrial
- Informações sobre SESI Viva+
- Serviços institucionais do SESI

### 📧 Envio de Documentos
- Capacidade de enviar documentos relevantes por e-mail
- Integração com SMTP para entrega de materiais

## 📋 Pré-requisitos

### Software Necessário
- **Java 17+** (JDK)
- **Maven 3.8+**
- **PostgreSQL 14+** com extensão PGVector
- **Ollama** instalado localmente
- **Conta Twilio** com número WhatsApp habilitado

### Conhecimentos Recomendados
- Spring Boot e Spring AI
- Conceitos de RAG e embeddings
- API REST
- PostgreSQL e SQL básico

## ⚙️ Guia de Instalação e Configuração

### 1️⃣ Clone o repositório

```bash
git clone https://github.com/MaViniciusDev/clara.git
cd clara
```

### 2️⃣ Configure o PostgreSQL com PGVector

#### Instalação do PostgreSQL (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

#### Instalação da extensão PGVector
```bash
# Clone o repositório do PGVector
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install
```

#### Criação do banco de dados
```sql
# Conecte ao PostgreSQL
sudo -u postgres psql

# Crie o banco e a extensão
CREATE DATABASE sesi_chatbot;
\c sesi_chatbot
CREATE EXTENSION vector;

# Crie um usuário (opcional)
CREATE USER clara_user WITH PASSWORD 'sua_senha_segura';
GRANT ALL PRIVILEGES ON DATABASE sesi_chatbot TO clara_user;
\q
```

### 3️⃣ Instale e configure o Ollama

```bash
# Instale o Ollama (Linux)
curl -fsSL https://ollama.ai/install.sh | sh

# Inicie o serviço Ollama
ollama serve

# Em outro terminal, baixe os modelos necessários
ollama pull llama3.2:latest
ollama pull nomic-embed-text

# Verifique se os modelos foram instalados
ollama list
```

### 4️⃣ Configure as credenciais da aplicação

```bash
# Copie o arquivo de exemplo
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edite o arquivo `application.properties` e configure:

#### 🗄️ Database
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sesi_chatbot
spring.datasource.username=clara_user
spring.datasource.password=sua_senha_segura
```

#### 📱 Twilio (WhatsApp)
Obtenha suas credenciais em: https://console.twilio.com
```properties
twilio.account.sid=SEU_ACCOUNT_SID
twilio.auth.token=SEU_AUTH_TOKEN
twilio.whatsapp.number=whatsapp:+SEU_NUMERO_TWILIO
```

#### 📧 Email (SMTP)
Configure com uma conta Gmail ou outro provedor:
```properties
spring.mail.username=seu_email@gmail.com
spring.mail.password=sua_senha_de_app
```

> ⚠️ **Para Gmail**: Use uma "Senha de App" ao invés da senha normal. 
> Gere em: https://myaccount.google.com/apppasswords

### 5️⃣ Compile e execute a aplicação

```bash
# Compile o projeto
./mvnw clean install

# Execute a aplicação
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:5000`

### 6️⃣ Configure o Webhook do Twilio

1. Acesse o [Console Twilio](https://console.twilio.com)
2. Navegue até **Messaging → Try it out → Send a WhatsApp message**
3. Configure o webhook para: `https://seu-dominio.com/webhook/twilio`
4. Utilize ngrok para teste local:
   ```bash
   ngrok http 5000
   # Use a URL gerada como webhook: https://XXXXX.ngrok.io/webhook/twilio
   ```

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

## 🧪 Testando a Aplicação

### Teste via API REST

```bash
# Teste de saúde da aplicação
curl http://localhost:5000/actuator/health

# Teste de processamento de mensagem (se endpoint disponível)
curl -X POST http://localhost:5000/api/test \
  -H "Content-Type: application/json" \
  -d '{"message": "Quais são os exames obrigatórios para mecânico?"}'
```

### Teste via WhatsApp

1. Adicione o número do Twilio Sandbox nos seus contatos
2. Envie a mensagem de ativação indicada pelo Twilio
3. Experimente perguntas como:
   - "O que é PCMSO?"
   - "Quais exames são necessários para soldador?"
   - "Como funciona o SESI Viva+?"
   - "Explique o fluxo do PGR"

## 🚀 Deploy em Produção

### Opção 1: Docker (Recomendado)

```bash
# Build da imagem
docker build -t clara-sesi .

# Execute com docker-compose
docker-compose up -d
```

### Opção 2: Servidor Linux

```bash
# Compile o JAR
./mvnw clean package -DskipTests

# Execute como serviço
java -jar target/clara-0.0.1-SNAPSHOT.jar
```

### Opção 3: Cloud (Heroku, AWS, Azure)

Configure as variáveis de ambiente e faça o deploy conforme a documentação do provedor.

## 🔧 Troubleshooting

### Erro ao conectar no PostgreSQL
```bash
# Verifique se o PostgreSQL está rodando
sudo systemctl status postgresql

# Verifique se a extensão PGVector está instalada
psql -d sesi_chatbot -c "SELECT * FROM pg_extension WHERE extname = 'vector';"
```

### Ollama não responde
```bash
# Verifique se o Ollama está rodando
curl http://localhost:11434/api/tags

# Reinicie o serviço
pkill ollama
ollama serve
```

### Twilio não recebe webhooks
- Certifique-se que a URL está acessível publicamente
- Use ngrok para testes locais
- Verifique os logs do Twilio Console

## 📊 Monitoramento

A aplicação expõe endpoints do Spring Boot Actuator:

- `/actuator/health` - Status da aplicação
- `/actuator/metrics` - Métricas de performance
- `/actuator/info` - Informações da aplicação

## 🎓 Sobre o Hackathon SESI Saúde

Este projeto foi desenvolvido como parte do **Hackathon SESI Saúde de Feira de Santana/BA**, um evento focado em inovação e tecnologia aplicadas à saúde ocupacional. 

### Objetivos do Hackathon
- Desenvolver soluções tecnológicas para melhorar o atendimento em saúde ocupacional
- Facilitar o acesso à informação para trabalhadores e empresas
- Modernizar processos e aumentar a eficiência dos serviços do SESI

### Diferenciais da Clara
✅ **Acessibilidade**: Interface via WhatsApp, plataforma já utilizada por milhões de brasileiros  
✅ **Precisão**: Respostas baseadas em documentação oficial através de RAG  
✅ **Disponibilidade**: 24/7, sem necessidade de agendamento prévio  
✅ **Escalabilidade**: Arquitetura moderna capaz de atender múltiplos usuários simultaneamente  
✅ **Open Source**: Código aberto para evolução contínua da comunidade  

## 🤝 Contribuindo

Contribuições são muito bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade X'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

### Ideias para Contribuição
- 🌐 Adicionar suporte a mais idiomas
- 📊 Dashboard de analytics e métricas de uso
- 🎯 Integração com outros canais (Telegram, Slack)
- 🔍 Melhorias no algoritmo de busca semântica
- 📱 Desenvolvimento de aplicativo mobile nativo

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Equipe

**Marcus Vinicius** - Desenvolvedor Full Stack
- GitHub: [@MaViniciusDev](https://github.com/MaViniciusDev)
- LinkedIn: [Marcus Vinicius](https://linkedin.com/in/maviniciusdev)

## 📞 Contato e Suporte

- **Issues**: [GitHub Issues](https://github.com/MaViniciusDev/clara/issues)
- **Discussões**: [GitHub Discussions](https://github.com/MaViniciusDev/clara/discussions)
- **Email**: sesihackathon@gmail.com

---

<div align="center">

**Desenvolvido com ❤️ para o Hackathon SESI Saúde - Feira de Santana/BA**

⭐ Se este projeto foi útil, considere dar uma estrela no GitHub!

</div>

