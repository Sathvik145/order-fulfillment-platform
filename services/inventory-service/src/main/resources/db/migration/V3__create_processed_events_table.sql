CREATE TABLE processed_events (
                                  event_id UUID PRIMARY KEY,
                                  order_id UUID NOT NULL,
                                  event_type VARCHAR(100) NOT NULL,
                                  processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);