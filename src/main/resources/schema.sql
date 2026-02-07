-- Database initialization script

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) UNIQUE NOT NULL,
    product_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create indexes for performance
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_product_id ON orders(product_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Insert sample data
INSERT INTO products (name, description, price, stock_count) VALUES
('iPhone 15 Pro', 'Latest iPhone with A17 Pro chip', 999.99, 100),
('Samsung Galaxy S24', 'Flagship Samsung phone', 899.99, 150),
('MacBook Pro M3', '14-inch MacBook Pro', 1999.99, 50),
('AirPods Pro 2', 'Wireless earbuds with ANC', 249.99, 200);

-- Display sample data
SELECT * FROM products;
