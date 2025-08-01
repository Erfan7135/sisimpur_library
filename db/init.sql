CREATE TYPE user_role AS ENUM ('ADMIN', 'USER');

-- Create User table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    role user_role NOT NULL DEFAULT 'USER'
);

-- Create Author table
CREATE TABLE authors (
    author_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    bio TEXT
);

-- Create Book table
CREATE TABLE books (
    book_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id INT NOT NULL,
    published_year INT,
    genre VARCHAR(100),
    in_stock INT DEFAULT 0,
    lend_count INT DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES authors(author_id) ON DELETE CASCADE
);

-- Create Lending table
CREATE TABLE lendings (
    lending_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    lending_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    return_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);

-- Insert sample users
INSERT INTO users (name, email, password_hash, role) VALUES
('Admin', 'admin@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ADMIN'), -- password: admin123
('Alice', 'alice@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'USER'), -- password: admin123
('Bob', 'bob@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'USER'); -- password: admin123

-- Insert sample authors
INSERT INTO authors (name, bio) VALUES
('J.K. Rowling', 'British author, best known for Harry Potter.'),
('George R.R. Martin', 'American novelist and creator of Game of Thrones.');

-- Insert sample books
INSERT INTO books (title, author_id, published_year, in_stock) VALUES
('Harry Potter and the Sorcerers Stone', 1, 1997, 5),
('Harry Potter and the Chamber of Secrets', 1, 1998, 5),
('A Game of Thrones', 2, 1996, 5),
('A Clash of Kings', 2, 1998, 5);

