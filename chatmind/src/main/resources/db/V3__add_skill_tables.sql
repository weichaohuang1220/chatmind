CREATE TABLE IF NOT EXISTS skill (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    tools JSONB NOT NULL DEFAULT '[]',
    trigger_keywords JSONB DEFAULT '[]',
    prompt_template TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add allowed_skills field to agent table
ALTER TABLE agent ADD COLUMN IF NOT EXISTS allowed_skills JSONB DEFAULT '[]';

-- Insert default skills
INSERT INTO skill (name, description, tools, trigger_keywords, prompt_template) VALUES
('智能问答', '从知识库中检索信息回答问题', '["KnowledgeTool"]', '["查询", "搜索", "知识库", "文档"]', '请从知识库中查找相关信息来回答用户的问题。'),
('联网搜索', '通过搜索引擎获取最新信息', '["WebSearchTool"]', '["搜索", "最新", "网上", "联网"]', '请通过网络搜索获取最新信息来回答用户的问题。'),
('数据分析', '查询数据库获取业务数据', '["DatabaseQueryTool"]', '["数据", "统计", "查询", "多少"]', '请查询数据库来获取所需的业务数据。')
ON CONFLICT (name) DO NOTHING;
