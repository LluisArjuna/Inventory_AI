ALTER TABLE inventories
    ADD COLUMN name VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN description TEXT;

CREATE INDEX idx_inventories_name ON inventories(name);
