-- V1__Initial_schema.sql
-- Create users table
CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Create inventories table
CREATE TABLE inventories (
    id BINARY(16) PRIMARY KEY,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BINARY(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_inventories_user_id ON inventories(user_id);

-- Create categories table
CREATE TABLE categories (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_name ON categories(name);

-- Create coordinates table
CREATE TABLE coordinates (
    id BINARY(16) PRIMARY KEY,
    coord_x DECIMAL(10, 6) NOT NULL,
    coord_y DECIMAL(10, 6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create items table
CREATE TABLE items (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    year INTEGER NOT NULL,
    inventory_id BINARY(16) NOT NULL,
    category_id BINARY(16) NOT NULL,
    coordinate_id BINARY(16),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_inventory FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_items_coordinate FOREIGN KEY (coordinate_id) REFERENCES coordinates(id)
);

CREATE INDEX idx_items_inventory_id ON items(inventory_id);
CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_items_coordinate_id ON items(coordinate_id);
CREATE INDEX idx_items_year ON items(year);

-- Create photos table
CREATE TABLE photos (
    id BINARY(16) PRIMARY KEY,
    item_id BINARY(16) NOT NULL,
    url TEXT NOT NULL,
    position INTEGER NOT NULL,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_photos_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

CREATE INDEX idx_photos_item_id ON photos(item_id);
CREATE INDEX idx_photos_position ON photos(item_id, position);