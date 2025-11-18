CREATE TABLE tickets (
    id VARCHAR(255) PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    restaurant_id VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL,
    ready_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket_line_items (
    id VARCHAR(255) PRIMARY KEY,
    ticket_id VARCHAR(255) NOT NULL,
    menu_item_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

CREATE INDEX idx_ticket_order ON tickets(order_id);
CREATE INDEX idx_ticket_restaurant ON tickets(restaurant_id);
CREATE INDEX idx_ticket_state ON tickets(state);
CREATE INDEX idx_line_item_ticket ON ticket_line_items(ticket_id);

