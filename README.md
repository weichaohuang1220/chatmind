<div align="center">

**English** | [简体中文](./README.zh-CN.md)

</div>

# ChatMind — AI Agent Assistant

> Last updated: 2026-03-22

ChatMind is a full-stack AI Agent chat application, built on a **Spring Boot 3.5.8 + Spring AI
1.1.0** backend and a **React + TypeScript + Ant Design X** frontend.

It is not a "chatbot" but an Agent: **it can plan, call tools, retrieve from a knowledge base, and
stream its execution process to the frontend in real time**.

The system uses a **Think-Execute loop, enabling it to understand complex tasks, plan execution
steps, invoke external tools, and retrieve relevant information from a knowledge base via RAG to
complete multi-step complex tasks**.

Project highlights:

* A Think-Execute loop (autonomous decision-making)
* A tool-calling framework (extensible)
* RAG + vector retrieval (pgvector)
* A multi-model switching architecture (registry pattern)
* SSE real-time push (execution state visualization)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend framework | Spring Boot 3.5.8, Spring AI 1.1.0 |
| Database | PostgreSQL + pgvector (vector retrieval) |
| ORM | MyBatis |
| Embedding model | bge-m3 (Ollama, localhost:11434) |
| LLM | DeepSeek Chat, GLM-4 (ZhipuAI) |
| Frontend framework | React 18, TypeScript, Vite |
| UI components | Ant Design 5, Ant Design X, Tailwind CSS |
| Real-time communication | SSE (Server-Sent Events) |

---

## Feature Overview

| # | Feature Module | Status | Created |
|---|---------------|--------|---------|
| 1 | Agent CRUD + multi-model | ✅ Done | 2026-03-22 |
| 2 | SSE real-time streaming output | ✅ Done | 2026-03-22 |
| 3 | ReAct Agent loop (run → plan → step → think → execute) | ✅ Done | 2026-03-22 |
| 4 | RAG knowledge base retrieval (pgvector + bge-m3) | ✅ Done | 2026-03-22 |
| 5 | Brave Search web search tool | ✅ Done | 2026-03-22 |
| 6 | Database Query tool | ✅ Done | 2026-03-22 |
| 7 | Skill orchestration system | ✅ Done | 2026-03-22 |
| 8 | Memory compression (sliding window + LLM summary) | ✅ Done | 2026-03-22 |
| 9 | Chunk overlap + metadata titles | ✅ Done | 2026-03-22 |
| 10 | Rerank (LLM-based) | ✅ Done | 2026-03-22 |
| 11 | Plan phase | ✅ Done | 2026-03-22 |
| 12 | Human-in-the-loop confirmation | ✅ Done | 2026-03-22 |
| 13 | Long-term memory (cross-session persistence) | ✅ Done | 2026-03-22 |
| 14 | QA evaluation + LLM-as-Judge | ✅ Done | 2026-03-22 |
| 15 | Logging across all endpoints (@Slf4j) | ✅ Done | 2026-03-22 |
| 16 | Frontend HITL confirmation dialog | ✅ Done | 2026-03-22 |
| 17 | Frontend Skill configuration panel | ✅ Done | 2026-03-22 |
| 18 | Frontend document reprocess button | ✅ Done | 2026-03-22 |

---

## Quick Start

### Requirements

| Dependency | Version | Notes |
|------------|---------|-------|
| JDK | 17+ | Backend compilation and runtime |
| Maven | 3.8+ | Backend build (or use the bundled `mvnw`) |
| Node.js | 18+ | Frontend build and runtime |
| npm | 9+ | Frontend package management |
| PostgreSQL | 15+ | Database; requires the pgvector extension |
| Ollama | latest | Runs the bge-m3 embedding model locally |

### Step 1: Prepare the database

```bash
# 1. Start PostgreSQL
brew services start postgresql  # macOS

# 2. Create the database
psql -U postgres -c "CREATE DATABASE chatmind;"

# 3. Install the pgvector extension
psql -U postgres -d chatmind -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

> Default connection settings (change them in `application.yaml`):
> - URL: `jdbc:postgresql://localhost:5432/chatmind`
> - Username: `postgres`
> - Password: `123456`

### Step 2: Start the Ollama embedding model

```bash
# 1. Install Ollama (if not already installed)
brew install ollama  # macOS

# 2. Start the Ollama service
ollama serve

# 3. Pull the bge-m3 embedding model (first time only)
ollama pull bge-m3
```

> Ollama runs on `http://localhost:11434` by default

### Step 3: Start the backend

```bash
# Enter the backend directory
cd chatmind

# Option 1: use Maven
mvn spring-boot:run

# Option 2: use the Maven Wrapper (no Maven installation needed)
./mvnw spring-boot:run

# Option 3: compile first, then run
mvn clean package -DskipTests
java -jar target/chatmind-0.0.1-SNAPSHOT.jar
```

> The backend runs on `http://localhost:8080` by default
>
> On first startup, SQL migrations run automatically (V3 Skill tables + V4 long-term memory table)

### Step 4: Start the frontend

```bash
# Enter the frontend directory
cd ui

# Install dependencies (first time only)
npm install

# Start in development mode
npm run dev
```

> The frontend runs on `http://localhost:5173` by default
>
> The frontend calls the backend REST API at `http://localhost:8080/api`
> The frontend opens an SSE connection at `http://localhost:8080/sse/connect/{sessionId}`

### Step 5: Verify the setup

```bash
# Check backend health
curl http://localhost:8080/api/agents
# Expected: {"code":200,"message":"success","data":{"agents":[]}}

# Check that Ollama is available
curl http://localhost:11434/api/tags
# Expected: a model list containing bge-m3
```

Open `http://localhost:5173` in a browser to start using ChatMind.

### Optional Configuration

| Setting | Location | Description |
|---------|----------|-------------|
| DeepSeek API Key | `application.yaml` → `spring.ai.deepseek.api-key` | Key for calling the DeepSeek model |
| ZhipuAI API Key | `application.yaml` → `spring.ai.zhipuai.api-key` | Key for calling the GLM-4 model |
| Brave Search API Key | `application.yaml` → `brave.api-key` | Web search (optional; if empty, the search tool returns a notice) |
| Mail SMTP | `application.yaml` → `spring.mail.*` | Email sending for the mail tool |
| Document storage path | `application.yaml` → `document.storage.base-path` | Local storage directory for uploaded documents, defaults to `./data/documents` |

### Running Tests

```bash
# Backend unit tests (83 of them)
cd chatmind
mvn test

# Frontend TypeScript type checking
cd ui
npx tsc --noEmit
```

---

## Detailed Feature Documentation

### 1. Agent CRUD + Multi-Model

**Description:** Full Agent management with create, read, update, and delete. Each Agent can be
configured independently with its own model, system prompt, tools, knowledge base, and skills.

**Multi-model architecture:**
- `@Bean("deepseek-chat")` — DeepSeek Chat model
- `@Bean("glm-4.6")` — ZhipuAI GLM-4 model
- `ChatClientRegistry` routes dynamically based on the Agent's configured `model` field

**API endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/agents` | Get all Agents |
| POST | `/api/agents` | Create an Agent |
| PATCH | `/api/agents/{agentId}` | Update an Agent |
| DELETE | `/api/agents/{agentId}` | Delete an Agent |

**Key files:**
- `controller/AgentController.java`
- `service/AgentFacadeService.java` → `impl/AgentFacadeServiceImpl.java`
- `model/entity/Agent.java`, `model/dto/AgentDTO.java`, `model/vo/AgentVO.java`
- `config/MultiChatClientConfig.java`, `config/ChatClientRegistry.java`

---

### 2. SSE Real-Time Streaming Output

**Description:** Uses Server-Sent Events to stream AI replies in real time. The frontend opens a
long-lived connection via EventSource, and the backend pushes token-level streaming data through an
SseEmitter.

**SSE message types:**
| Type | Description |
|------|-------------|
| `AI_TOKEN` | A single streamed token |
| `AI_GENERATED_CONTENT` | Full generated content (including tool call information) |
| `AI_PLANNING` | Agent planning phase status |
| `AI_THINKING` | Agent thinking phase status |
| `AI_EXECUTING` | Agent executing phase status |
| `AI_DONE` | Agent finished |
| `AWAITING_CONFIRMATION` | Awaiting user confirmation (HITL) |

**Connection flow:**
1. Frontend calls `new EventSource("/sse/connect/{chatSessionId}")`
2. Backend creates an `SseEmitter` and stores it in `ConcurrentHashMap<chatSessionId, emitter>`
3. An `init` event is sent to tell the frontend the handshake is complete
4. Subsequent AI responses are pushed through that emitter

**Key files:**
- `controller/SseController.java`
- `service/SseService.java` → `impl/SseServiceImpl.java`
- `message/SseMessage.java`
- `ui/src/components/views/AgentChatView.tsx` (frontend EventSource listener)

---

### 3. ReAct Agent Loop

**Description:** Implements an Agent loop following the ReAct (Reasoning + Acting) paradigm. After
receiving user input, the Agent goes through an iterative **plan → think → execute** loop until it
produces a final answer.

**Loop flow:**
```
run()
  ├── injectLongTermMemory()    // Inject cross-session memory
  ├── plan()                     // Plan the execution steps
  └── step() × N (up to maxIterations)
        ├── compressMemoryIfNeeded()  // Compress the context
        ├── think()                    // LLM reasoning, decides which tool to call
        └── execute()                  // Execute the tool call
              ├── isHighRiskTool()?    // HITL check
              │     └── AWAITING_CONFIRMATION → wait for user confirmation
              └── ToolCallingManager.executeToolCalls()
  finally:
      persistLongTermMemory()         // Extract and persist key information
```

**State management:**
- `AgentState` enum: `IDLE → PLANNING → THINKING → EXECUTING → FINISHED`
- Every state change is pushed to the frontend in real time via SSE

**Key files:**
- `agent/ChatMind.java` — core Agent implementation
- `agent/ChatMindFactory.java` — Agent factory, injects all dependencies
- `agent/AgentState.java` — state enum

---

### 4. RAG Knowledge Base Retrieval

**Description:** A complete RAG (Retrieval-Augmented Generation) pipeline supporting Markdown
document upload, automatic chunking, vectorization, and similarity retrieval.

**RAG pipeline:**
```
Markdown upload → chunking (200-char overlap) → bge-m3 embedding → pgvector storage
                                                              ↓
User query → bge-m3 embedding → pgvector similarity search (top-5) → Rerank (top-3) → inject into prompt
```

**Key techniques:**
- **Vector database:** PostgreSQL + the pgvector extension, using the `<->` L2 distance operator
- **Embedding model:** bge-m3 (deployed locally via Ollama)
- **Chunk overlap:** adjacent chunks overlap by 200 characters to prevent semantic breaks
- **Metadata titles:** each chunk stores its corresponding Markdown heading; retrieval results are
  shown as `【Title】content`
- **Rerank:** recall top-5 first, then use an LLM to rerank and select the top-3

**API endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/knowledge-bases` | Get all knowledge bases |
| POST | `/api/knowledge-bases` | Create a knowledge base |
| PATCH | `/api/knowledge-bases/{id}` | Update a knowledge base |
| DELETE | `/api/knowledge-bases/{id}` | Delete a knowledge base |
| GET | `/api/documents` | Get all documents |
| GET | `/api/documents/kb/{kbId}` | Get documents in a knowledge base |
| POST | `/api/documents/upload` | Upload a document |
| POST | `/api/documents/{id}/reprocess` | Reprocess a document |
| DELETE | `/api/documents/{id}` | Delete a document |

**Key files:**
- `service/RagService.java` → `impl/RagServiceImpl.java` — vector retrieval + embedding
- `service/impl/DocumentFacadeServiceImpl.java` — document processing, chunk splitting
- `service/RerankService.java` → `impl/RerankServiceImpl.java` — LLM reranking
- `agent/tools/KnowledgeTools.java` — knowledge base retrieval tool called by the Agent
- `mapper/ChunkBgeM3Mapper.java` + `ChunkBgeM3Mapper.xml` — pgvector SQL

---

### 5. Brave Search Web Search Tool

**Description:** Gives the Agent web search capability through the Brave Search API, so it can fall
back to web search when RAG finds nothing.

**Implementation:**
- Calls the Brave Search API using `RestClient`
- Auto-registered via `@Component`, marked `ToolType.OPTIONAL`
- Returns a friendly notice rather than an error when no API key is configured
- Returns the top-5 search results (title + description + URL)

**Configuration:**
```yaml
brave:
  api-key: your-api-key-here  # application.yaml
```

**Key files:**
- `agent/tools/WebSearchTools.java`

---

### 6. Database Query Tool

**Description:** The Agent can query the business database directly, supporting natural-language-to-SQL
queries.

**Safety measures:**
- Regex filtering: rejects dangerous operations such as INSERT/UPDATE/DELETE/DROP/ALTER/TRUNCATE/CREATE
- Only SELECT and WITH/CTE queries are permitted
- Automatically appends `LIMIT 50` to prevent oversized responses
- Error messages do not expose stack traces

**Key files:**
- `agent/tools/DataBaseTools.java`

---

### 7. Skill Orchestration System

**Description:** A Skill is a higher-level abstraction over a Tool. Tools are atomic capabilities;
Skills are orchestrated combinations of Tools. Agents select the Skills they need, allowing flexible
capability configuration.

**Data model:**
```
Skill
├── id (UUID)
├── name (skill name)
├── description (description)
├── tools (JSON Array: names of the associated tools)
├── triggerKeywords (JSON Array: trigger keywords)
└── promptTemplate (prompt template)
```

**Built-in skills:**
| Skill | Included Tools |
|-------|---------------|
| Intelligent Q&A | KnowledgeTool |
| Web Search | WebSearchTool |
| Data Analysis | DatabaseQueryTool |

**API endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/skills` | Get all skills |
| GET | `/api/skills/{id}` | Get skill details |
| POST | `/api/skills` | Create a skill |
| PATCH | `/api/skills/{id}` | Update a skill |
| DELETE | `/api/skills/{id}` | Delete a skill |

**Agent association:**
- Agents gain an `allowedSkills` field (JSON array) storing the list of permitted Skill IDs

**Key files:**
- `model/entity/Skill.java`, `model/dto/SkillDTO.java`, `model/vo/SkillVO.java`
- `controller/SkillController.java`
- `service/SkillFacadeService.java` → `impl/SkillFacadeServiceImpl.java`
- `mapper/SkillMapper.java` + `SkillMapper.xml`
- `converter/SkillConverter.java`
- `resources/db/V3__add_skill_tables.sql`

---

### 8. Memory Compression (Sliding Window + LLM Summary)

**Description:** Solves context explosion. Once a conversation exceeds 15 messages, older messages
are automatically compressed into an LLM-generated summary while the 5 most recent exchanges are
kept in full.

**Compression strategy:**
```
message count ≤ 15 → no compression, full context retained
message count > 15 → old messages (first N-5) → LLM summary → [summary] + [5 most recent]
```

**Key files:**
- `service/MemoryCompressionService.java` → `impl/MemoryCompressionServiceImpl.java`
- `agent/ChatMind.java` — the `compressMemoryIfNeeded()` method

---

### 9. Chunk Overlap + Metadata Titles

**Description:** An improved document chunking strategy that addresses semantic loss at chunk
boundaries.

**Chunk overlap:**
- Adjacent chunks share a 200-character overlap region
- From the second chunk onward, the last 200 characters of the previous chunk are prepended

**Metadata titles:**
- Each chunk stores the Markdown heading it belongs to (e.g. `{"title":"孙悟空"}`)
- Retrieval results are shown as `【孙悟空】chunk content...`, helping the LLM understand where the
  context came from

**Key files:**
- `service/impl/DocumentFacadeServiceImpl.java` — chunking logic
- `service/impl/RagServiceImpl.java` — retrieval result formatting

---

### 10. Rerank (LLM-based)

**Description:** Two-stage retrieval: first recall the top-5 candidates by vector similarity, then
have an LLM rerank them and select the 3 most relevant.

**Reranking flow:**
1. pgvector vector retrieval → top-5 candidates
2. Build a rerank prompt → send to the LLM
3. The LLM returns a reordered index list
4. Take the top-3 as the final retrieval result

**Fault tolerance:**
- If the LLM returns a malformed response, fall back to the original ordering (take the first 3)

**Key files:**
- `service/RerankService.java` → `impl/RerankServiceImpl.java`
- `agent/tools/KnowledgeTools.java` — invokes rerank

---

### 11. Plan Phase

**Description:** For complex questions, the Agent generates an execution plan before acting. The
plan is pushed to the frontend for display as an `AI_PLANNING` SSE message.

**Trigger conditions:**
- The user message contains a question mark (?/？)
- The user message is longer than 20 characters

**Key files:**
- `agent/ChatMind.java` — the `plan()` and `needsPlanning()` methods

---

### 12. Human-in-the-Loop Confirmation

**Description:** Before executing a high-risk tool (such as a database query or sending email), the
Agent pauses and pushes a confirmation request over SSE, waiting for the user to approve or reject
it in a frontend dialog.

**Confirmation flow:**
```
Agent.execute() → isHighRiskTool()?
  → Yes → push AWAITING_CONFIRMATION over SSE
  → frontend shows a confirmation dialog
  → user clicks Approve / Reject
  → POST /sse/confirm/{confirmationId}
  → CompletableFuture.complete()
  → Agent continues / skips
```

**High-risk tools:**
- `DatabaseQueryTool` — database queries
- `EmailTool` — sending email

**Timeout behavior:**
- Automatically rejected after 60 seconds with no response, and the pending state is cleaned up

**Key files:**
- `service/ConfirmationService.java` → `impl/ConfirmationServiceImpl.java`
- `message/SseMessage.java` — the `AWAITING_CONFIRMATION` type
- `controller/SseController.java` — `POST /sse/confirm/{confirmationId}`
- `ui/src/components/views/AgentChatView.tsx` — confirmation modal

---

### 13. Long-Term Memory (Cross-Session Persistence)

**Description:** After an Agent session ends, the LLM automatically extracts key user information
(name, preferences, facts) and persists it to the database. It is injected automatically when a new
session begins, giving the Agent memory across sessions.

**Memory extraction:**
- `persistLongTermMemory()` is called in the `finally` block when the Agent's `run()` completes
- The LLM extracts key:value pairs from the conversation (e.g. `user_name:张三`)
- PostgreSQL `ON CONFLICT DO UPDATE` implements the UPSERT

**Memory injection:**
- `injectLongTermMemory()` is called when the Agent's `run()` begins
- All memories are formatted into a SystemMessage prepended to the conversation

**Data model:**
```sql
CREATE TABLE long_term_memory (
  id UUID PRIMARY KEY,
  agent_id VARCHAR(64) NOT NULL,
  memory_key VARCHAR(255) NOT NULL,
  memory_value TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(agent_id, memory_key)
);
```

**Key files:**
- `model/entity/LongTermMemory.java`
- `mapper/LongTermMemoryMapper.java` + `LongTermMemoryMapper.xml`
- `service/LongTermMemoryService.java` → `impl/LongTermMemoryServiceImpl.java`
- `resources/db/V4__add_long_term_memory.sql`

---

### 14. QA Evaluation + LLM-as-Judge

**Description:** An automated RAG quality evaluation system supporting Recall@3 and MRR metrics plus
LLM semantic scoring.

**Evaluation metrics:**
| Metric | Description |
|--------|-------------|
| Recall@3 | The proportion of top-3 retrieval results containing the expected content |
| MRR (Mean Reciprocal Rank) | The average of the reciprocal rank of the first correct result |
| LLM-as-Judge Score | The LLM's relevance score for retrieval results against the question (0-1) |

**Evaluation dataset:**
- `resources/evaluation/qa-dataset.json` — 50 QA test cases covering all 15 chunks

**API endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/evaluation/run?kbId=xxx` | Basic evaluation (Recall + MRR) |
| POST | `/api/evaluation/run-with-judge?kbId=xxx` | Evaluation including LLM-as-Judge |

**Key files:**
- `service/EvaluationService.java` → `impl/EvaluationServiceImpl.java`
- `controller/EvaluationController.java`
- `model/dto/EvaluationResult.java`, `EvaluationReport.java`

---

### 15. Logging Across All Endpoints

**Description:** All 9 Controllers carry the `@Slf4j` annotation and a standardized try-catch logging
pattern.

**Logging pattern:**
```java
@Slf4j
public class XxxController {
    public ApiResponse<T> method() {
        log.info("[XxxController] method called, params...");
        try {
            // business logic
            log.info("[XxxController] method success, result...");
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("[XxxController] method failed, params...", e);
            throw e; // handled centrally by GlobalExceptionHandler
        }
    }
}
```

**Controllers covered:**
- AgentController, ChatMessageController, ChatSessionController
- DocumentController, EvaluationController, KnowledgeBaseController
- SkillController, SseController, ToolController

---

### 16. Frontend HITL Confirmation Dialog

**Description:** When the Agent requests execution of a high-risk tool, the frontend opens a
confirmation dialog where the user can approve or reject it.

**UI interaction:**
1. An `AWAITING_CONFIRMATION` message arrives over SSE
2. An Ant Design Modal opens showing the tool name and arguments
3. The user clicks "Approve" or "Reject"
4. The frontend calls `POST /sse/confirm/{confirmationId}` with the result

**Key files:**
- `ui/src/components/views/AgentChatView.tsx`
- `ui/src/types/index.ts` — added the `AWAITING_CONFIRMATION` type
- `ui/src/api/api.ts` — added the `confirmAction()` function

---

### 17. Frontend Skill Configuration Panel

**Description:** The Agent create/edit modal gains a "Skill Configuration" tab where Skills can be
selected for an Agent.

**UI interaction:**
- A "Skill Configuration" menu item was added to AddAgentModal
- The list shows all available Skills with their name, description, and tool tags
- Skills can be checked and unchecked
- The `allowedSkills` field is submitted on save

**Key files:**
- `ui/src/components/modals/AddAgentModal.tsx`
- `ui/src/api/api.ts` — added Skill API types and functions

---

### 18. Frontend Document Reprocess Button

**Description:** Each document in the knowledge base document list gains a "Reprocess" button that
triggers re-chunking and re-vectorization on the backend.

**Use cases:**
- Chunk generation failed after the document was first uploaded
- The chunking strategy changed and chunks need regenerating
- The vector index needs rebuilding

**Key files:**
- `ui/src/components/views/KnowledgeBaseView.tsx`
- `ui/src/api/api.ts` — added the `reprocessDocument()` function

---

## Database Migrations

| File | Description |
|------|-------------|
| `V3__add_skill_tables.sql` | Creates the skill table; adds the allowed_skills (jsonb) column to the Agent table |
| `V4__add_long_term_memory.sql` | Creates the long_term_memory table with UNIQUE(agent_id, memory_key) |

---

## Test Coverage

| Test File | Tests | Description |
|-----------|-------|-------------|
| AgentControllerTest | 8 | Agent CRUD, all endpoints, happy path + error cases |
| ChatMessageControllerTest | 8 | Message CRUD, all endpoints, happy path + error cases |
| ChatSessionControllerTest | 10 | Session CRUD, all endpoints, happy path + error cases |
| DocumentControllerTest | 12 | Document CRUD + upload + reprocess, happy path + error cases |
| EvaluationControllerTest | 4 | Evaluation + LLM-as-Judge, happy path + error cases |
| KnowledgeBaseControllerTest | 9 | Knowledge base CRUD, all endpoints, happy path + error cases |
| SkillControllerTest | 10 | Skill CRUD, all endpoints, happy path + error cases |
| SseControllerTest | 5 | SSE connection + HITL confirmation, happy path + error cases |
| ToolControllerTest | 3 | Tool listing, happy path + error cases |
| **Total** | **83** | **All passing ✅** |

---

## Project Structure

```
ChatMind-main/
├── README.md             # Full project documentation (this file)
│
├── chatmind/            # Backend (Spring Boot)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/kama/chatmind/
│       │   ├── agent/           # Agent core + tools
│       │   │   ├── ChatMind.java
│       │   │   ├── ChatMindFactory.java
│       │   │   ├── AgentState.java
│       │   │   └── tools/       # Tool implementations
│       │   │       ├── KnowledgeTools.java
│       │   │       ├── WebSearchTools.java
│       │   │       ├── DataBaseTools.java
│       │   │       ├── EmailTools.java
│       │   │       └── ...
│       │   ├── config/          # Configuration classes
│       │   ├── controller/      # REST controllers (10)
│       │   ├── converter/       # DTO/VO converters
│       │   ├── event/           # Event listeners
│       │   ├── exception/       # Global exception handling
│       │   ├── mapper/          # MyBatis mappers
│       │   ├── message/         # SSE message definitions
│       │   ├── model/           # Entities/DTOs/VOs/requests/responses
│       │   ├── service/         # Service layer (interfaces + implementations)
│       │   └── typehandler/     # MyBatis type handlers
│       ├── main/resources/
│       │   ├── application.yaml
│       │   ├── mapper/          # MyBatis XML
│       │   ├── db/              # SQL migrations
│       │   └── evaluation/      # QA evaluation dataset
│       └── test/                # Unit tests (83)
│
└── ui/                   # Frontend (React + TypeScript)
    ├── package.json
    └── src/
        ├── api/             # API request wrappers
        ├── components/      # UI components
        │   ├── modals/      # Modals
        │   ├── tabs/        # Tab content
        │   └── views/       # Main views
        ├── contexts/        # React Context
        ├── hooks/           # Custom hooks
        ├── layout/          # Layout components
        ├── types/           # TypeScript types
        └── utils/           # Utility functions
```

---

## Development Log (CHANGELOG)

### 2026-03-22 Feature Iteration (Driven by Interview Feedback)

#### Round 1: Core Tools + Skill System

**1. Brave Search Tool (web search) ✅**
- Calls the Brave Search API using RestClient
- Auto-registered via `@Component`, marked `ToolType.OPTIONAL`
- Returns a notice rather than an error when no API key is configured
- Returns the top-5 search results (title + description + URL)

> Interview talking point:
> "My Agent can not only query a local knowledge base but also search the web. When RAG finds
> nothing, it falls back to Brave Search automatically."

**2. Database Query Tool ✅**
- Injects JdbcTemplate and executes read-only SQL
- Safety limits: regex rejection of INSERT/UPDATE/DELETE/DROP/ALTER/TRUNCATE/CREATE and other
  dangerous operations
- Supports WITH/CTE queries and strips trailing semicolons automatically
- Automatically appends LIMIT 50 to prevent oversized responses

> Interview talking point:
> "The Agent can query the business database directly — ask 'how many Agents are there?' in natural
> language and it returns the result."

**3. Skill Orchestration System ✅**
- Added 11 files (entity, DTO, VO, request/response, Converter, Mapper, Service, Controller, SQL
  migration)
- Modified 7 files (Agent-related, adding the allowedSkills field)
- Three built-in skills: Intelligent Q&A, Web Search, Data Analysis

> Interview talking point:
> "I made Skills configurable — Tools are atomic capabilities, Skills are orchestrations of Tools.
> Agents select the Skills they need."

#### Round 2: RAG Enhancements + Memory System

**4. Memory Compression (Sliding Window + Summary) ✅**
- Threshold of 15 messages; keeps the 5 most recent and calls the LLM to summarize older messages

> Interview talking point:
> "Past 15 messages it compresses automatically — older conversation is replaced by a generated
> summary while the 5 most recent exchanges stay intact."

**5. Chunk Overlap + Metadata Titles ✅**
- Adjacent chunks overlap by 200 characters; metadata stores the title `{"title":"孙悟空"}`
- Retrieval results are prefixed with the title: `【孙悟空】content...`

> Interview talking point:
> "Chunks overlap by 200 characters to prevent semantic breaks, and metadata stores the title so the
> LLM understands where the context came from."

**6. Rerank + Plan Phase ✅**
- LLM-based reranking: retrieve top-5 first, then rerank down to top-3
- The `plan()` method: complex questions are planned before execution

> Interview talking point:
> "RAG retrieval recalls 5 results first, then an LLM reranks and picks 3, improving accuracy. For
> complex questions the Agent generates an execution plan before acting."

#### Round 3: Safety + Evaluation + Long-Term Memory

**7. Human-in-the-Loop + QA Evaluation ✅**
- Confirmation mechanism: CompletableFuture pauses execution, the frontend shows a confirmation
  dialog
- RAG evaluation: automatic assessment via Recall@3 and MRR metrics
- 5 sets of QA test data

> Interview talking point:
> "Before a high-risk tool runs, an AWAITING_CONFIRMATION event is pushed and the frontend shows a
> confirmation dialog — execution only continues after the user approves. RAG quality is assessed
> automatically using Recall@3 and MRR."

**8. Long-Term Memory (Cross-Session Persistence) ✅**
- Long-term memory is injected when the Agent's run() begins, and the LLM extracts and persists key
  information when it ends
- Uses the PostgreSQL UPSERT mechanism

> Interview talking point:
> "When the Agent finishes running, it calls the LLM to extract key user information (name,
> preferences, facts) and stores it in the database. The next session injects it automatically,
> giving memory across sessions."

**9. QA Evaluation Expansion + LLM-as-Judge ✅**
- Test data grew from 5 to 50 cases, covering all 15 chunks
- Added LLM-as-Judge: DeepSeek scores retrieval results (0-1)
- Added the `POST /api/evaluation/run-with-judge` endpoint

> Interview talking point:
> "I built a 50-case QA test set with automatic Recall@3 and MRR assessment. I also added
> LLM-as-Judge, using DeepSeek to score retrieval results from 0 to 1 to evaluate retrieval quality."

#### Bug Fix Log

| File | Fix |
|------|-----|
| `EvaluationServiceImpl.java` | InputStream resource leak → try-with-resources |
| `EvaluationServiceImpl.java` | Empty QA entries caused NPE → skip with a warning |
| `DocumentFacadeServiceImpl.java` | System.out.println → log.debug |
| `RagServiceImpl.java` | Vector retrieval returned null → Collections.emptyList() |
| `SkillFacadeServiceImpl.java` | Mapper returned null → fall back to an empty list |
| `ConfirmationServiceImpl.java` | No timeout mechanism → orTimeout(60s) + automatic cleanup |
| `RagServiceImpl.java` | WebClient polluted by Spring AI → fixed |
| `DocumentController.java` | Added the document reprocess endpoint |
| `ChunkBgeM3Mapper.java` | Added deleteByDocId |

#### Verification Results

- Compilation: ✅ BUILD SUCCESS
- Tests: ✅ 83/83 passing
- TypeScript: ✅ Type checking passes
- SQL migrations: ✅ V3 Skill tables + V4 long_term_memory table created

---

## Project Highlights

**1. A Real Agent Loop (Think-Execute Loop + State Machine)**

Not "call the LLM once and stop" — it supports:
* Multi-round planning
* Multi-round tool calling
* State management (THINKING / EXECUTING / DONE / ERROR)
* Error handling and a maximum step count (preventing infinite loops)

Technical angles: "How do you stop an Agent from calling tools forever? How do you manage state? How
do you handle timeouts?"

**2. Tool System (Fixed + Optional Tools, Extensible and Governable)**

ChatMind's tool system is built as a framework:
* Tools register themselves automatically
* Fixed and optional tools are managed as separate categories
* Extensible: adding a tool does not change the core flow
* Controllable: Spring AI's automatic execution is disabled in favor of manually managing the
  ToolCalling flow

Technical angles: "How do you extend tool calling? What happens when a tool fails? How do tool
results enter the conversation history?"

**3. RAG Knowledge Base (PostgreSQL + pgvector)**

RAG here is not a slide-deck concept — ChatMind implements the full pipeline:
* Markdown document parsing and chunking (with 200-character overlap)
* Embedding generation and storage
* pgvector similarity retrieval (`<->`)
* ivfflat index optimization, supporting 100k+ vectors
* LLM reranking (top-5 → top-3)

The key point: PostgreSQL alone manages both structured and vector data (simple deployment, low
cost, good transactional consistency).

**4. Multi-Model Support (ChatClientRegistry Registry Pattern)**

* Switchable between DeepSeek and ZhipuAI
* A unified ChatClient interface
* Registry pattern for managing model instances (decoupling creation from use)
* Easy to extend with more models later

**5. SSE Real-Time Communication (Live Execution Visualization)**

ChatMind uses SSE to deliver:
* Real-time state push: THINKING / EXECUTING / DONE
* A live view of "what the Agent is doing right now"
* Simpler than WebSocket and well suited to one-way push

Technical angles: What's the difference between SSE and WebSocket? How are connections managed? How
are timeouts handled? How does it hold up under concurrency?

**6. Safe and Controllable (Human-in-the-Loop + SQL Injection Protection)**

* High-risk tools require user confirmation before executing
* The database tool filters dangerous SQL with regexes
* CompletableFuture + automatic rejection after a 60-second timeout
* End-to-end log tracing

---

## What You'll Learn From This Project

* AI Agent fundamentals: the Think-Execute loop (multi-round planning + multi-round tool calling),
  state machines, and timeout/error handling
* Tool-calling systems: an extensible tool framework (fixed/optional tools), tool registration and
  dispatch, and manually taking over Spring AI's tool execution flow
* The full RAG pipeline: Markdown parsing and chunking → embedding storage → pgvector similarity
  retrieval (index optimization, SQL tuning)
* Multi-model architecture design: the ChatClientRegistry registry pattern, supporting dynamic
  switching and extension across DeepSeek, ZhipuAI, and others
* Backend engineering skills: Spring Boot layered architecture, RESTful APIs, unified
  exception/response handling, complex MyBatis SQL, and custom TypeHandlers (vector)
* Real-time communication: SSE server push, connection management, and live execution state display
* Expressing quantifiable results: how to build, write up, and talk about metrics like "<2s
  response", "100+ concurrent", and "85%+ retrieval accuracy" that an interviewer grasps instantly

---

## Future Improvements

| Direction | Description |
|-----------|-------------|
| MCP Server integration | Go MCP Server → Spring AI MCP Client |
| Multi-file async embedding | Process large volumes of documents in parallel |
| Session-UserID binding | Multi-user isolation |
| Load testing | Concurrency performance assessment |
| Multi-Agent collaboration | Multiple Agents cooperating on complex tasks |
