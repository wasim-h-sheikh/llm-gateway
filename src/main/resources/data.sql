-- ---------------------------------------------------------------------------
-- First-time model seeding.
--
-- Runs automatically on application launch (Spring SQL init) AFTER Hibernate
-- has created/updated the schema (spring.jpa.defer-datasource-initialization=true).
--
-- Idempotent: model_name is UNIQUE, so ON CONFLICT DO NOTHING means rows are
-- only inserted the first time and every subsequent startup is a no-op.
--
-- Values mirror the `llm models:create` CLI commands.
-- ---------------------------------------------------------------------------

INSERT INTO model_metadata (
    model_name, provider, provider_bean_name,
    input_cost_per1k, output_cost_per1k,
    avg_latency_ms, context_window,
    supports_streaming, supports_tools,
    enabled, priority, health_score,
    total_requests, successful_requests, failed_requests,
    average_response_time_ms, timeout_count,
    created_at, updated_at
) VALUES
    ('gpt-4o-mini',     'OPENAI',    'openAiProvider', 0.00015, 0.00060, 600,  128000,  TRUE, TRUE, TRUE, 1, 100.0, 0, 0, 0, 0.0, 0, NOW(), NOW()),
    ('gemini-2.5-flash','GOOGLE',    'geminiProvider', 0.00030, 0.00250, 500,  1048576, TRUE, TRUE, TRUE, 1, 100.0, 0, 0, 0, 0.0, 0, NOW(), NOW()),
    ('claude-haiku-4-5','ANTHROPIC', 'claudeProvider', 0.00100, 0.00500, 400,  200000,  TRUE, TRUE, TRUE, 1, 100.0, 0, 0, 0, 0.0, 0, NOW(), NOW())
ON CONFLICT (model_name) DO NOTHING;
