CREATE TABLE inventory_reservations (
                                        id UUID PRIMARY KEY,

                                        order_id UUID NOT NULL,

                                        product_id VARCHAR(100) NOT NULL,

                                        quantity INTEGER NOT NULL,

                                        status VARCHAR(50) NOT NULL,

                                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                        CONSTRAINT uq_inventory_reservation_order_product
                                            UNIQUE (order_id, product_id)
);