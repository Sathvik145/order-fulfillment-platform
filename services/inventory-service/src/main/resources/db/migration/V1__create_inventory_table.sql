CREATE TABLE inventory (
                           product_id VARCHAR(100) PRIMARY KEY,
                           available_quantity INTEGER NOT NULL,
                           reserved_quantity INTEGER NOT NULL DEFAULT 0,
                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);