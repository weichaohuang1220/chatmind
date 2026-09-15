<div align="center">

[English](./README.md) | **简体中文**

</div>

# AI智能体助手-ChatMind

> 最后更新：2026-03-22

ChatMind 是一个全栈 AI Agent 聊天应用，基于 **Spring Boot 3.5.8 + Spring AI 1.1.0** 后端和 **React + TypeScript + Ant Design X** 前端构建。

它不是"聊天机器人"，而是 Agent：**能规划、能调用工具、能检索知识库、还能把执行过程实时推给前端**。

系统采用 **Think-Execute 循环机制，能够理解复杂任务、规划执行步骤、调用外部工具，并基于 RAG 技术从知识库中检索相关信息，完成多步骤的复杂任务**。

项目重点亮点：

* 我实现了 Think-Execute 循环（自主决策）
* 我实现了 工具调用框架（可扩展）
* 我实现了 RAG + 向量检索（pgvector）
* 我实现了 多模型切换架构（注册表模式）
* 我实现了 SSE 实时推送（执行状态可视化）

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.8, Spring AI 1.1.0 |
| 数据库 | PostgreSQL + pgvector (向量检索) |
| ORM | MyBatis |
| 向量模型 | bge-m3 (Ollama, localhost:11434) |
| LLM | DeepSeek Chat, GLM-4 (ZhipuAI) |
| 前端框架 | React 18, TypeScript, Vite |
| UI 组件 | Ant Design 5, Ant Design X, Tailwind CSS |
| 实时通信 | SSE (Server-Sent Events) |

---

## 功能模块一览

| # | 功能模块 | 状态 | 创建日期 |
|---|---------|------|---------|
| 1 | Agent CRUD + 多模型 | ✅ 已完成 | 2026-03-22 |
| 2 | SSE 实时流式输出 | ✅ 已完成 | 2026-03-22 |
| 3 | ReAct Agent 循环 (run → plan → step → think → execute) | ✅ 已完成 | 2026-03-22 |
| 4 | RAG 知识库检索 (pgvector + bge-m3) | ✅ 已完成 | 2026-03-22 |
| 5 | Brave Search 联网搜索工具 | ✅ 已完成 | 2026-03-22 |
| 6 | Database Query 数据库查询工具 | ✅ 已完成 | 2026-03-22 |
| 7 | Skill 技能编排系统 | ✅ 已完成 | 2026-03-22 |
| 8 | 记忆压缩（滑动窗口 + LLM 摘要） | ✅ 已完成 | 2026-03-22 |
| 9 | Chunk 重叠 + Metadata 标题 | ✅ 已完成 | 2026-03-22 |
| 10 | Rerank 重排序 (LLM-based) | ✅ 已完成 | 2026-03-22 |
| 11 | Plan 规划阶段 | ✅ 已完成 | 2026-03-22 |
| 12 | Human-in-the-loop 人工确认 | ✅ 已完成 | 2026-03-22 |
| 13 | 长期记忆（跨会话持久化） | ✅ 已完成 | 2026-03-22 |
| 14 | QA 评测 + LLM-as-Judge | ✅ 已完成 | 2026-03-22 |
| 15 | 全接口日志 (@Slf4j) | ✅ 已完成 | 2026-03-22 |
| 16 | 前端 HITL 确认框 | ✅ 已完成 | 2026-03-22 |
| 17 | 前端 Skill 配置面板 | ✅ 已完成 | 2026-03-22 |
| 18 | 前端文档重处理按钮 | ✅ 已完成 | 2026-03-22 |

---

## 快速启动

### 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端编译运行 |
| Maven | 3.8+ | 后端构建（或使用项目自带 `mvnw`） |
| Node.js | 18+ | 前端构建运行 |
| npm | 9+ | 前端包管理 |
| PostgreSQL | 15+ | 数据库，需安装 pgvector 扩展 |
| Ollama | latest | 本地运行 bge-m3 嵌入模型 |

### 第一步：数据库准备

```bash
# 1. 启动 PostgreSQL
brew services start postgresql  # macOS

# 2. 创建数据库
psql -U postgres -c "CREATE DATABASE chatmind;"

# 3. 安装 pgvector 扩展
psql -U postgres -d chatmind -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

> 默认连接信息（可在 `application.yaml` 修改）：
> - URL: `jdbc:postgresql://localhost:5432/chatmind`
> - 用户名: `postgres`
> - 密码: `123456`

### 第二步：启动 Ollama 嵌入模型

```bash
# 1. 安装 Ollama（如未安装）
brew install ollama  # macOS

# 2. 启动 Ollama 服务
ollama serve

# 3. 拉取 bge-m3 嵌入模型（首次需要）
ollama pull bge-m3
```

> Ollama 默认运行在 `http://localhost:11434`

### 第三步：启动后端

```bash
# 进入后端目录
cd chatmind

# 方式一：使用 Maven
mvn spring-boot:run

# 方式二：使用 Maven Wrapper（无需安装 Maven）
./mvnw spring-boot:run

# 方式三：先编译再运行
mvn clean package -DskipTests
java -jar target/chatmind-0.0.1-SNAPSHOT.jar
```

> 后端默认运行在 `http://localhost:8080`
>
> 首次启动会自动执行 SQL 迁移（V3 Skill 表 + V4 长期记忆表）

### 第四步：启动前端

```bash
# 进入前端目录
cd ui

# 安装依赖（首次需要）
npm install

# 开发模式启动
npm run dev
```

> 前端默认运行在 `http://localhost:5173`
>
> 前端通过 `http://localhost:8080/api` 调用后端 REST API
> 前端通过 `http://localhost:8080/sse/connect/{sessionId}` 建立 SSE 连接

### 第五步：验证启动

```bash
# 检查后端健康
curl http://localhost:8080/api/agents
# 期望返回：{"code":200,"message":"success","data":{"agents":[]}}

# 检查 Ollama 可用
curl http://localhost:11434/api/tags
# 期望返回包含 bge-m3 的模型列表
```

打开浏览器访问 `http://localhost:5173`，即可使用 ChatMind。

### 可选配置

| 配置项 | 位置 | 说明 |
|--------|------|------|
| DeepSeek API Key | `application.yaml` → `spring.ai.deepseek.api-key` | DeepSeek 模型调用密钥 |
| ZhipuAI API Key | `application.yaml` → `spring.ai.zhipuai.api-key` | GLM-4 模型调用密钥 |
| Brave Search API Key | `application.yaml` → `brave.api-key` | 联网搜索功能（可选，为空则搜索工具返回提示） |
| 邮件 SMTP | `application.yaml` → `spring.mail.*` | 邮件工具发送功能 |
| 文档存储路径 | `application.yaml` → `document.storage.base-path` | 上传文档的本地存储目录，默认 `./data/documents` |

### 运行测试

```bash
# 后端单元测试（83 个）
cd chatmind
mvn test

# 前端 TypeScript 类型检查
cd ui
npx tsc --noEmit
```

---

## 详细功能说明

### 1. Agent CRUD + 多模型

**描述：** 完整的 Agent 智能体管理，支持创建、查询、更新、删除。每个 Agent 可独立配置模型、系统提示词、工具、知识库和技能。

**多模型架构：**
- `@Bean("deepseek-chat")` — DeepSeek Chat 模型
- `@Bean("glm-4.6")` — 智谱 GLM-4 模型
- 通过 `ChatClientRegistry` 根据 Agent 配置的 model 字段动态路由

**API 端点：**
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/agents` | 获取所有 Agent |
| POST | `/api/agents` | 创建 Agent |
| PATCH | `/api/agents/{agentId}` | 更新 Agent |
| DELETE | `/api/agents/{agentId}` | 删除 Agent |

**核心文件：**
- `controller/AgentController.java`
- `service/AgentFacadeService.java` → `impl/AgentFacadeServiceImpl.java`
- `model/entity/Agent.java`, `model/dto/AgentDTO.java`, `model/vo/AgentVO.java`
- `config/MultiChatClientConfig.java`, `config/ChatClientRegistry.java`

---

### 2. SSE 实时流式输出

**描述：** 使用 Server-Sent Events 实现 AI 回复的实时流式推送。前端通过 EventSource 建立长连接，后端通过 SseEmitter 推送 token 级别的流式数据。

**SSE 消息类型：**
| 类型 | 描述 |
|------|------|
| `AI_TOKEN` | 单个 token 流式推送 |
| `AI_GENERATED_CONTENT` | 完整生成内容（含 tool call 信息） |
| `AI_PLANNING` | Agent 规划阶段状态 |
| `AI_THINKING` | Agent 思考阶段状态 |
| `AI_EXECUTING` | Agent 执行阶段状态 |
| `AI_DONE` | Agent 完成 |
| `AWAITING_CONFIRMATION` | 等待用户确认（HITL） |

**连接流程：**
1. 前端 `new EventSource("/sse/connect/{chatSessionId}")`
2. 后端创建 `SseEmitter`，存入 `ConcurrentHashMap<chatSessionId, emitter>`
3. 发送 `init` 事件通知前端握手完成
4. 后续 AI 响应通过该 emitter 推送

**核心文件：**
- `controller/SseController.java`
- `service/SseService.java` → `impl/SseServiceImpl.java`
- `message/SseMessage.java`
- `ui/src/components/views/AgentChatView.tsx`（前端 EventSource 监听）

---

### 3. ReAct Agent 循环

**描述：** 实现了 ReAct（Reasoning + Acting）范式的 Agent 循环。Agent 收到用户输入后，经历 **计划 → 思考 → 执行** 的迭代循环，直到产出最终回答。

**循环流程：**
```
run()
  ├── injectLongTermMemory()    // 注入跨会话记忆
  ├── plan()                     // 规划执行计划
  └── step() × N (最多 maxIterations)
        ├── compressMemoryIfNeeded()  // 压缩上下文
        ├── think()                    // LLM 推理，决定调用哪个工具
        └── execute()                  // 执行工具调用
              ├── isHighRiskTool()?    // HITL 检查
              │     └── AWAITING_CONFIRMATION → 等待用户确认
              └── ToolCallingManager.executeToolCalls()
  finally:
      persistLongTermMemory()         // 提取关键信息持久化
```

**状态管理：**
- `AgentState` 枚举：`IDLE → PLANNING → THINKING → EXECUTING → FINISHED`
- 每次状态变化通过 SSE 实时推送给前端

**核心文件：**
- `agent/ChatMind.java` — Agent 核心实现
- `agent/ChatMindFactory.java` — Agent 工厂，注入所有依赖
- `agent/AgentState.java` — 状态枚举

---

### 4. RAG 知识库检索

**描述：** 完整的 RAG (Retrieval-Augmented Generation) 流水线，支持 Markdown 文档上传、自动分块、向量化、相似度检索。

**RAG 流水线：**
```
Markdown 上传 → 分段(含200字重叠) → bge-m3 Embedding → pgvector 存储
                                                              ↓
用户查询 → bge-m3 Embedding → pgvector 相似度检索(top-5) → Rerank(top-3) → 注入 Prompt
```

**关键技术：**
- **向量数据库：** PostgreSQL + pgvector 扩展，使用 `<->` L2 距离算子
- **嵌入模型：** bge-m3 (Ollama 本地部署)
- **Chunk 重叠：** 相邻段落重叠 200 字符，防止语义断裂
- **Metadata 标题：** 每个 chunk 存储对应的 Markdown 标题，检索结果展示为 `【标题】内容`
- **Rerank：** 先召回 top-5，再用 LLM 重排序选出 top-3

**API 端点：**
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/knowledge-bases` | 获取所有知识库 |
| POST | `/api/knowledge-bases` | 创建知识库 |
| PATCH | `/api/knowledge-bases/{id}` | 更新知识库 |
| DELETE | `/api/knowledge-bases/{id}` | 删除知识库 |
| GET | `/api/documents` | 获取所有文档 |
| GET | `/api/documents/kb/{kbId}` | 获取知识库下文档 |
| POST | `/api/documents/upload` | 上传文档 |
| POST | `/api/documents/{id}/reprocess` | 重新处理文档 |
| DELETE | `/api/documents/{id}` | 删除文档 |

**核心文件：**
- `service/RagService.java` → `impl/RagServiceImpl.java` — 向量检索 + Embedding
- `service/impl/DocumentFacadeServiceImpl.java` — 文档处理、Chunk 切分
- `service/RerankService.java` → `impl/RerankServiceImpl.java` — LLM 重排序
- `agent/tools/KnowledgeTools.java` — Agent 调用的知识库检索工具
- `mapper/ChunkBgeM3Mapper.java` + `ChunkBgeM3Mapper.xml` — pgvector SQL

---

### 5. Brave Search 联网搜索工具

**描述：** Agent 通过 Brave Search API 实现联网搜索能力，RAG 查不到时可 fallback 到网络搜索。

**实现方式：**
- 使用 `RestClient` 调用 Brave Search API
- `@Component` 自动注册，`ToolType.OPTIONAL`（可选工具）
- 无 API Key 时返回友好提示，不会报错
- 返回 top-5 搜索结果（标题 + 描述 + URL）

**配置：**
```yaml
brave:
  api-key: your-api-key-here  # application.yaml
```

**核心文件：**
- `agent/tools/WebSearchTools.java`

---

### 6. Database Query 数据库查询工具

**描述：** Agent 可以直接查询业务数据库，支持自然语言转 SQL 查询。

**安全措施：**
- 正则过滤：拒绝 INSERT/UPDATE/DELETE/DROP/ALTER/TRUNCATE/CREATE 等危险操作
- 仅允许 SELECT 和 WITH/CTE 查询
- 自动追加 `LIMIT 50` 防止超大响应
- 错误信息不暴露堆栈

**核心文件：**
- `agent/tools/DataBaseTools.java`

---

### 7. Skill 技能编排系统

**描述：** Skill 是 Tool 的更高层抽象。Tool 是原子能力，Skill 是 Tool 的编排组合。Agent 按需勾选 Skill，实现灵活的能力配置。

**数据模型：**
```
Skill
├── id (UUID)
├── name (技能名称)
├── description (描述)
├── tools (JSON Array: 关联的工具名列表)
├── triggerKeywords (JSON Array: 触发关键词)
└── promptTemplate (提示词模板)
```

**预置技能：**
| 技能 | 包含工具 |
|------|---------|
| 智能问答 | KnowledgeTool |
| 联网搜索 | WebSearchTool |
| 数据分析 | DatabaseQueryTool |

**API 端点：**
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/skills` | 获取所有技能 |
| GET | `/api/skills/{id}` | 获取技能详情 |
| POST | `/api/skills` | 创建技能 |
| PATCH | `/api/skills/{id}` | 更新技能 |
| DELETE | `/api/skills/{id}` | 删除技能 |

**Agent 关联：**
- Agent 新增 `allowedSkills` 字段（JSON 数组），存储允许使用的 Skill ID 列表

**核心文件：**
- `model/entity/Skill.java`, `model/dto/SkillDTO.java`, `model/vo/SkillVO.java`
- `controller/SkillController.java`
- `service/SkillFacadeService.java` → `impl/SkillFacadeServiceImpl.java`
- `mapper/SkillMapper.java` + `SkillMapper.xml`
- `converter/SkillConverter.java`
- `resources/db/V3__add_skill_tables.sql`

---

### 8. 记忆压缩（滑动窗口 + LLM 摘要）

**描述：** 解决上下文爆炸问题。当对话消息超过 15 条时，自动将旧消息压缩为 LLM 生成的摘要，保留最近 5 条完整对话。

**压缩策略：**
```
消息数 ≤ 15 → 不压缩，保持完整上下文
消息数 > 15 → 旧消息(前N-5条) → LLM 生成摘要 → [摘要] + [最近5条]
```

**核心文件：**
- `service/MemoryCompressionService.java` → `impl/MemoryCompressionServiceImpl.java`
- `agent/ChatMind.java` — `compressMemoryIfNeeded()` 方法

---

### 9. Chunk 重叠 + Metadata 标题

**描述：** 改进文档分块策略，解决 chunk 边界处的语义丢失问题。

**Chunk 重叠：**
- 相邻 chunk 之间有 200 字符的重叠区域
- 从第 2 个 chunk 开始，前置上一个 chunk 末尾 200 字符

**Metadata 标题：**
- 每个 chunk 存储其所属的 Markdown 标题（如 `{"title":"孙悟空"}`）
- 检索结果展示为 `【孙悟空】chunk内容...`，帮助 LLM 理解上下文来源

**核心文件：**
- `service/impl/DocumentFacadeServiceImpl.java` — 分块逻辑
- `service/impl/RagServiceImpl.java` — 检索结果格式化

---

### 10. Rerank 重排序 (LLM-based)

**描述：** 两阶段检索：先用向量相似度召回 top-5 候选，再用 LLM 对候选结果重排序选出最相关的 top-3。

**重排序流程：**
1. pgvector 向量检索 → top-5 候选
2. 构建 Rerank Prompt → 传入 LLM
3. LLM 返回排序后的索引列表
4. 取 top-3 作为最终检索结果

**容错机制：**
- 如果 LLM 返回格式异常，fallback 到原始排序（取前 3 条）

**核心文件：**
- `service/RerankService.java` → `impl/RerankServiceImpl.java`
- `agent/tools/KnowledgeTools.java` — 调用 Rerank

---

### 11. Plan 规划阶段

**描述：** 对复杂问题，Agent 在执行前先生成执行计划。计划通过 SSE 推送 `AI_PLANNING` 类型消息给前端展示。

**触发条件：**
- 用户消息包含问号（?/？）
- 用户消息长度 > 20 字符

**核心文件：**
- `agent/ChatMind.java` — `plan()` 和 `needsPlanning()` 方法

---

### 12. Human-in-the-loop 人工确认

**描述：** 高危工具（如数据库查询、邮件发送）执行前，Agent 暂停并通过 SSE 推送确认请求，等待用户在前端弹窗中批准或拒绝。

**确认流程：**
```
Agent.execute() → isHighRiskTool()?
  → Yes → SSE 推送 AWAITING_CONFIRMATION
  → 前端弹出确认框
  → 用户点击 批准/拒绝
  → POST /sse/confirm/{confirmationId}
  → CompletableFuture.complete()
  → Agent 继续/跳过
```

**高危工具：**
- `DatabaseQueryTool` — 数据库查询
- `EmailTool` — 邮件发送

**超时机制：**
- 60 秒无响应自动拒绝，并清理 pending 状态

**核心文件：**
- `service/ConfirmationService.java` → `impl/ConfirmationServiceImpl.java`
- `message/SseMessage.java` — `AWAITING_CONFIRMATION` 类型
- `controller/SseController.java` — `POST /sse/confirm/{confirmationId}`
- `ui/src/components/views/AgentChatView.tsx` — 确认 Modal

---

### 13. 长期记忆（跨会话持久化）

**描述：** Agent 会话结束后，LLM 自动提取用户关键信息（名字、偏好、事实）持久化到数据库。新会话开始时自动注入，实现跨会话记忆。

**记忆提取：**
- Agent `run()` 结束时，在 `finally` 块中调用 `persistLongTermMemory()`
- LLM 从对话中提取 key:value 对（如 `user_name:张三`）
- 使用 PostgreSQL `ON CONFLICT DO UPDATE` 实现 UPSERT

**记忆注入：**
- Agent `run()` 开始时调用 `injectLongTermMemory()`
- 将所有记忆格式化为 SystemMessage 前置到对话

**数据模型：**
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

**核心文件：**
- `model/entity/LongTermMemory.java`
- `mapper/LongTermMemoryMapper.java` + `LongTermMemoryMapper.xml`
- `service/LongTermMemoryService.java` → `impl/LongTermMemoryServiceImpl.java`
- `resources/db/V4__add_long_term_memory.sql`

---

### 14. QA 评测 + LLM-as-Judge

**描述：** 自动化 RAG 质量评估系统，支持 Recall@3、MRR 指标和 LLM 语义评分。

**评测指标：**
| 指标 | 描述 |
|------|------|
| Recall@3 | top-3 检索结果中包含期望内容的比例 |
| MRR (Mean Reciprocal Rank) | 首个正确结果的排名倒数的平均值 |
| LLM-as-Judge Score | LLM 对检索结果与问题相关性的评分 (0-1) |

**评测数据集：**
- `resources/evaluation/qa-dataset.json` — 50 条 QA 测试数据，覆盖全部 15 个 chunk

**API 端点：**
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/evaluation/run?kbId=xxx` | 基础评测 (Recall + MRR) |
| POST | `/api/evaluation/run-with-judge?kbId=xxx` | 含 LLM-as-Judge 评测 |

**核心文件：**
- `service/EvaluationService.java` → `impl/EvaluationServiceImpl.java`
- `controller/EvaluationController.java`
- `model/dto/EvaluationResult.java`, `EvaluationReport.java`

---

### 15. 全接口日志

**描述：** 所有 9 个 Controller 均添加了 `@Slf4j` 注解和标准化的 try-catch 日志模式。

**日志模式：**
```java
@Slf4j
public class XxxController {
    public ApiResponse<T> method() {
        log.info("[XxxController] method called, params...");
        try {
            // 业务逻辑
            log.info("[XxxController] method success, result...");
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("[XxxController] method failed, params...", e);
            throw e; // GlobalExceptionHandler 统一处理
        }
    }
}
```

**覆盖的 Controller：**
- AgentController, ChatMessageController, ChatSessionController
- DocumentController, EvaluationController, KnowledgeBaseController
- SkillController, SseController, ToolController

---

### 16. 前端 HITL 确认框

**描述：** 当 Agent 请求执行高危工具时，前端弹出确认对话框，用户可以批准或拒绝。

**UI 交互：**
1. SSE 收到 `AWAITING_CONFIRMATION` 消息
2. 弹出 Ant Design Modal 显示工具名称和参数
3. 用户点击「批准执行」或「拒绝」
4. 前端调用 `POST /sse/confirm/{confirmationId}` 发送确认结果

**核心文件：**
- `ui/src/components/views/AgentChatView.tsx`
- `ui/src/types/index.ts` — 新增 `AWAITING_CONFIRMATION` 类型
- `ui/src/api/api.ts` — 新增 `confirmAction()` 函数

---

### 17. 前端 Skill 配置面板

**描述：** Agent 创建/编辑模态框中新增「技能配置」标签页，支持为 Agent 勾选可用的技能组合。

**UI 交互：**
- AddAgentModal 中新增「技能配置」菜单项
- 列表展示所有可用 Skill，显示名称、描述和包含的工具标签
- 勾选/取消勾选 Skill
- 保存时提交 `allowedSkills` 字段

**核心文件：**
- `ui/src/components/modals/AddAgentModal.tsx`
- `ui/src/api/api.ts` — 新增 Skill API 类型和函数

---

### 18. 前端文档重处理按钮

**描述：** 知识库文档列表中，每个文档新增「重新处理」按钮，触发后端重新切分和向量化。

**使用场景：**
- 文档首次上传后 chunk 生成失败
- 切分策略调整后需要重新生成
- 向量索引需要重建

**核心文件：**
- `ui/src/components/views/KnowledgeBaseView.tsx`
- `ui/src/api/api.ts` — 新增 `reprocessDocument()` 函数

---

## 数据库迁移

| 文件 | 描述 |
|------|------|
| `V3__add_skill_tables.sql` | 创建 skill 表，Agent 表新增 allowed_skills (jsonb) 列 |
| `V4__add_long_term_memory.sql` | 创建 long_term_memory 表，含 UNIQUE(agent_id, memory_key) |

---

## 测试覆盖

| 测试文件 | 测试数 | 描述 |
|---------|--------|------|
| AgentControllerTest | 8 | Agent CRUD 全接口正向+异常 |
| ChatMessageControllerTest | 8 | 消息 CRUD 全接口正向+异常 |
| ChatSessionControllerTest | 10 | 会话 CRUD 全接口正向+异常 |
| DocumentControllerTest | 12 | 文档 CRUD + 上传 + 重处理 正向+异常 |
| EvaluationControllerTest | 4 | 评测 + LLM-as-Judge 正向+异常 |
| KnowledgeBaseControllerTest | 9 | 知识库 CRUD 全接口正向+异常 |
| SkillControllerTest | 10 | 技能 CRUD 全接口正向+异常 |
| SseControllerTest | 5 | SSE 连接 + HITL 确认 正向+异常 |
| ToolControllerTest | 3 | 工具列表 正向+异常 |
| **合计** | **83** | **全部通过 ✅** |

---

## 项目结构

```
ChatMind-main/
├── README.md             # 项目完整文档（本文件）
│
├── chatmind/            # 后端 (Spring Boot)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/kama/chatmind/
│       │   ├── agent/           # Agent 核心 + 工具
│       │   │   ├── ChatMind.java
│       │   │   ├── ChatMindFactory.java
│       │   │   ├── AgentState.java
│       │   │   └── tools/       # 工具实现
│       │   │       ├── KnowledgeTools.java
│       │   │       ├── WebSearchTools.java
│       │   │       ├── DataBaseTools.java
│       │   │       ├── EmailTools.java
│       │   │       └── ...
│       │   ├── config/          # 配置类
│       │   ├── controller/      # REST 控制器 (10个)
│       │   ├── converter/       # DTO/VO 转换器
│       │   ├── event/           # 事件监听
│       │   ├── exception/       # 全局异常处理
│       │   ├── mapper/          # MyBatis Mapper
│       │   ├── message/         # SSE 消息定义
│       │   ├── model/           # 实体/DTO/VO/请求/响应
│       │   ├── service/         # 服务层 (接口 + 实现)
│       │   └── typehandler/     # MyBatis 类型处理器
│       ├── main/resources/
│       │   ├── application.yaml
│       │   ├── mapper/          # MyBatis XML
│       │   ├── db/              # SQL 迁移
│       │   └── evaluation/      # QA 评测数据集
│       └── test/                # 单元测试 (83个)
│
└── ui/                   # 前端 (React + TypeScript)
    ├── package.json
    └── src/
        ├── api/             # API 请求封装
        ├── components/      # UI 组件
        │   ├── modals/      # 模态框
        │   ├── tabs/        # 标签页内容
        │   └── views/       # 主视图
        ├── contexts/        # React Context
        ├── hooks/           # 自定义 Hooks
        ├── layout/          # 布局组件
        ├── types/           # TypeScript 类型
        └── utils/           # 工具函数
```

---

## 开发日志 (CHANGELOG)

### 2026-03-22 功能迭代（面试反馈驱动）

#### 第一轮：核心工具 + Skill 系统

**1. Brave Search Tool（联网搜索）✅**
- 使用 RestClient 调用 Brave Search API
- `@Component` 自动注册，`ToolType.OPTIONAL`
- 无 API Key 时返回提示信息，不会报错
- 返回 top-5 搜索结果（标题+描述+URL）

> 面试话术：
> "我的 Agent 不仅能查本地知识库，还能联网搜索。RAG 查不到时自动 fallback 到 Brave Search。"

**2. Database Query Tool（数据库查询）✅**
- 注入 JdbcTemplate，执行只读 SQL
- 安全限制：正则拒绝 INSERT/UPDATE/DELETE/DROP/ALTER/TRUNCATE/CREATE 等危险操作
- 支持 WITH/CTE 查询，自动剥离尾部分号
- 自动追加 LIMIT 50 防止超大响应

> 面试话术：
> "Agent 可以直接查询业务数据库，用自然语言问'有多少个 Agent'就能返回结果。"

**3. Skill 编排系统 ✅**
- 新增 11 个文件（实体、DTO、VO、请求/响应、Converter、Mapper、Service、Controller、SQL 迁移）
- 修改 7 个文件（Agent 相关，新增 allowedSkills 字段）
- 预置 3 个技能：智能问答、联网搜索、数据分析

> 面试话术：
> "我实现了 Skill 可配置化——Tool 是原子能力，Skill 是 Tool 的编排。Agent 按需勾选 Skill。"

#### 第二轮：RAG 增强 + 记忆系统

**4. 记忆压缩（滑动窗口 + 摘要）✅**
- 阈值 15 条消息，保留最近 5 条，旧消息调 LLM 生成摘要

> 面试话术：
> "超过 15 条消息时自动压缩，旧对话生成摘要替换原始记录，保留最近 5 轮完整对话。"

**5. Chunk 重叠 + Metadata 标题 ✅**
- 相邻 chunk 重叠 200 字符；metadata 存储标题 `{"title":"孙悟空"}`
- 检索结果前置标题 `【孙悟空】content...`

> 面试话术：
> "chunk 之间有 200 字重叠防止语义断裂，metadata 存标题便于 LLM 理解上下文来源。"

**6. Rerank 重排序 + Plan 规划 ✅**
- LLM-based 重排序：先检索 top-5，再 Rerank 取 top-3
- `plan()` 方法：复杂问题先规划再执行

> 面试话术：
> "RAG 检索先召回 5 条，用 LLM 重排序选 3 条，提升准确率。Agent 对复杂问题先生成执行计划再行动。"

#### 第三轮：安全 + 评测 + 长期记忆

**7. Human-in-the-loop + QA 评测 ✅**
- 确认机制：CompletableFuture 暂停执行，前端弹窗确认
- RAG 评测：Recall@3 和 MRR 指标自动评估
- 5 组 QA 测试数据

> 面试话术：
> "高危工具执行前推 AWAITING_CONFIRMATION 事件，前端弹确认框，用户批准后才继续。RAG 评测用 Recall@3 和 MRR 指标自动评估检索质量。"

**8. 长期记忆（跨会话持久化）✅**
- Agent run() 开始注入长期记忆，结束时 LLM 提取关键信息持久化
- PostgreSQL UPSERT 机制

> 面试话术：
> "Agent 运行结束后自动调 LLM 提取用户关键信息（名字、偏好、事实）存到数据库。下次会话自动注入，实现跨会话记忆。"

**9. QA 评测扩展 + LLM-as-Judge ✅**
- 5 条 → 50 条测试数据，覆盖全部 15 个 chunk
- 新增 LLM-as-Judge：DeepSeek 对检索结果打分（0-1）
- 新增 `POST /api/evaluation/run-with-judge` 端点

> 面试话术：
> "构建了 50 条 QA 测试集，支持 Recall@3 和 MRR 指标自动评估。还加了 LLM-as-Judge，用 DeepSeek 对检索结果打分（0-1），评估检索质量。"

#### Bug 修复记录

| 文件 | 修复内容 |
|------|---------|
| `EvaluationServiceImpl.java` | InputStream 资源泄漏 → try-with-resources |
| `EvaluationServiceImpl.java` | 空 QA 条目导致 NPE → 跳过并警告 |
| `DocumentFacadeServiceImpl.java` | System.out.println → log.debug |
| `RagServiceImpl.java` | 向量检索返回 null → Collections.emptyList() |
| `SkillFacadeServiceImpl.java` | Mapper 返回 null → 空列表兜底 |
| `ConfirmationServiceImpl.java` | 无超时机制 → orTimeout(60s) + 自动清理 |
| `RagServiceImpl.java` | WebClient 被 Spring AI 污染 → 修复 |
| `DocumentController.java` | 添加文档重处理端点 |
| `ChunkBgeM3Mapper.java` | 添加 deleteByDocId |

#### 验证结果

- 编译：✅ BUILD SUCCESS
- 测试：✅ 83/83 通过
- TypeScript：✅ 类型检查通过
- SQL 迁移：✅ V3 Skill 表 + V4 long_term_memory 表已创建

---

## 项目亮点

**1. 真正的 Agent Loop（Think-Execute 循环 + 状态机）**

不是"调用一次大模型就结束"，而是支持：
* 多轮规划
* 多轮工具调用
* 状态管理（THINKING / EXECUTING / DONE / ERROR）
* 错误处理与最大步数控制（防止无限循环）

技术点："怎么避免 Agent 无限调用工具？怎么做状态管理？怎么做超时控制？"

**2. 工具系统（固定工具 + 可选工具，可扩展、可治理）**

ChatMind 的工具系统是"框架化"的：
* 工具自动注册
* 固定工具 / 可选工具分类管理
* 可扩展：新增工具不改核心流程
* 可控：禁用 Spring AI 自动执行，改为手动管理 ToolCalling 流程

技术点："工具调用怎么做扩展？工具失败怎么处理？工具返回结果怎么进入对话历史？"

**3. RAG 知识库（PostgreSQL + pgvector）**

RAG 不是 PPT 概念，ChatMind 是完整链路：
* Markdown 文档解析、分块（含 200 字重叠）
* Embedding 生成并落库
* pgvector 相似度检索（<->）
* ivfflat 索引优化，支持 10 万+向量
* LLM Rerank 重排序（top-5 → top-3）

最关键的点：用 PostgreSQL 一套体系把结构化数据和向量数据都管了（部署简单、成本低、事务一致性好）

**4. 多模型支持（注册表模式 ChatClientRegistry）**

* DeepSeek / 智谱 AI 可切换
* 统一 ChatClient 接口
* 注册表模式管理模型实例（解耦创建与使用）
* 便于未来扩展更多模型

**5. SSE 实时通信（执行过程实时可视化）**

ChatMind 用 SSE 做了：
* 状态实时推送：THINKING / EXECUTING / DONE
* 前端能实时看到"Agent 正在干啥"
* 比 WebSocket 更简单，适合单向推送

技术点：SSE 和 WebSocket 区别？连接怎么管理？超时怎么处理？并发怎么扛？

**6. 安全可控（Human-in-the-loop + SQL 注入防护）**

* 高危工具执行前必须用户确认
* 数据库工具正则过滤危险 SQL
* CompletableFuture + 60 秒超时自动拒绝
* 全链路日志追踪

---

## 学完本项目可以掌握什么？

* AI Agent 核心：Think-Execute 循环（多轮规划 + 多轮工具调用）+ 状态机 + 超时/错误处理
* 工具调用体系：可扩展工具框架（固定/可选工具）、工具注册与调度、手动接管 Spring AI 工具执行流程
* RAG 全链路：Markdown 解析与分块 → Embedding 入库 → pgvector 相似度检索（索引优化、SQL 调优）
* 多模型架构设计：ChatClientRegistry 注册表模式，支持 DeepSeek/智谱等模型动态切换与扩展
* 后端工程能力：Spring Boot 分层架构、RESTful API、统一异常/响应、MyBatis 复杂 SQL + 自定义 TypeHandler（vector）
* 实时通信：SSE 服务端推送、连接管理、执行状态实时展示
* 可量化成果表达：响应 <2s、并发 100+、检索准确率 85%+ 这种"面试官一眼懂"的指标怎么做、怎么写、怎么讲

---

## 未来改进方向

| 方向 | 描述 |
|------|------|
| MCP Server 集成 | Go MCP Server → Spring AI MCP Client |
| 多文件异步 Embedding | 并行处理大量文档 |
| Session-UserID 绑定 | 多用户隔离 |
| 压力测试 | 并发性能评估 |
| Multi-Agent 协作 | 多 Agent 协同完成复杂任务 |

