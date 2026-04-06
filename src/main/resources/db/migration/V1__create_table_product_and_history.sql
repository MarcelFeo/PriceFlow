CREATE TABLE produtos (
    url   VARCHAR(1000) NOT NULL,
    email VARCHAR(255)  NOT NULL,
    name  VARCHAR(255)  NOT NULL,

    CONSTRAINT pk_produtos PRIMARY KEY (url, email)
);

CREATE TABLE historico_preco (
     id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY ,
     price NUMERIC(19,2),
     captured_at TIMESTAMP,

     product_url   VARCHAR(1000) NOT NULL,
     product_email VARCHAR(255)  NOT NULL,

     CONSTRAINT fk_historico_produto
         FOREIGN KEY (product_url, product_email)
             REFERENCES produtos (url, email)
             ON DELETE CASCADE
);
