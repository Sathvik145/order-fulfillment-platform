CREATE TABLE products (
                          product_id VARCHAR(100) PRIMARY KEY,
                          name VARCHAR(200) NOT NULL,
                          description VARCHAR(1000),
                          price NUMERIC(12, 2) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);