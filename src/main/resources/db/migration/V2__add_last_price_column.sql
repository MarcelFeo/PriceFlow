-- Migration referente ao PriceCheckScheduler
-- Adiciona coluna last_price para rastrear a última mudança de preço

ALTER TABLE produtos ADD COLUMN last_price NUMERIC(19, 2) NULL;

-- Índice para melhorar performance nas buscas
CREATE INDEX idx_produto_email ON produtos(email);
CREATE INDEX idx_preco_captured_at ON historico_preco(captured_at);
