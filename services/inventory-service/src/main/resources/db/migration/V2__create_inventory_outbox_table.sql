CREATE TABLE inventory_outbox (
                                  id UUID PRIMARY KEY,
                                  aggregate_id UUID NOT NULL,
                                  event_type VARCHAR(100) NOT NULL,
                                  payload TEXT NOT NULL,
                                  status VARCHAR(50) NOT NULL,
                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  published_at TIMESTAMP WITH TIME ZONE
);