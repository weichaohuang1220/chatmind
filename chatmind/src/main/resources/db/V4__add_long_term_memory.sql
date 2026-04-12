CREATE TABLE IF NOT EXISTS long_term_memory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id UUID NOT NULL,
    chat_session_id VARCHAR(100),
    memory_key VARCHAR(200) NOT NULL,
    memory_value TEXT NOT NULL,
    category VARCHAR(50) DEFAULT 'fact',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (agent_id, memory_key)
);

CREATE INDEX IF NOT EXISTS idx_ltm_agent_id ON long_term_memory(agent_id);
