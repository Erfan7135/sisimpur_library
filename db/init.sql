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
('Admin', 'admin@example.com', '$2a$10$9dfZmj9mc.2HIYGz6h.ELOei72wnlZknHLtP1mg3ssgy2OyRiw7ze', 'ADMIN'), -- password: admin123
('Alice', 'alice@example.com', '$2a$10$9dfZmj9mc.2HIYGz6h.ELOei72wnlZknHLtP1mg3ssgy2OyRiw7ze', 'USER'), -- password: admin123
('Bob', 'bob@example.com', '$2a$10$9dfZmj9mc.2HIYGz6h.ELOei72wnlZknHLtP1mg3ssgy2OyRiw7ze', 'USER'); -- password: admin123

-- Insert sample authors
INSERT INTO authors (name, bio) VALUES
('J.R.R. Tolkien', 'British author, best known for The Lord of the Rings.'),
('Agatha Christie', 'British writer known for her detective novels, particularly those featuring Hercule Poirot and Miss Marple.'),
('Isaac Asimov', 'American author and professor of biochemistry, known for his works on science fiction and popular science.'),
('J.K. Rowling', 'British author, best known for Harry Potter.'),
('George R.R. Martin', 'American novelist and creator of Game of Thrones.');

-- Insert sample books
INSERT INTO books (title, author_id, published_year, genre, in_stock,lend_count) VALUES
('Harry Potter and the Sorcerers Stone', 1, 1997, 'Fantasy', 0, 2),
('Harry Potter and the Chamber of Secrets', 1, 1998, 'Fantasy', 4, 1),
('A Game of Thrones', 2, 1996, 'Fantasy', 3, 1),
('A Clash of Kings', 2, 1998, 'Fantasy', 1, 1),
('The Fellowship of the Ring', 3, 1954, 'Fantasy', 2, 1),
('The Two Towers', 3, 1954, 'Fantasy', 5, 0),
('The Return of the King', 3, 1955, 'Fantasy', 5, 0),
('Foundation', 4, 1951, 'Science Fiction', 5, 0),
('I, Robot', 4, 1950, 'Science Fiction', 5, 0),
('The Caves of Steel', 4, 1954, 'Science Fiction', 5, 0),
('The Naked Sun', 4, 1957, 'Science Fiction', 5, 0),
('The Two Towers', 3, 1954, 'Fantasy', 5, 0),
('Foundation', 4, 1951, 'Science Fiction', 5, 0),
('I, Robot', 4, 1950, 'Science Fiction', 5, 0),
('The Caves of Steel', 4, 1954, 'Science Fiction', 5, 0),
('The Naked Sun', 4, 1957, 'Science Fiction', 5, 0),
('Murder on the Orient Express', 5, 1934, 'Mystery', 5, 0),
('And Then There Were None', 5, 1939, 'Mystery', 5, 0);

-- Insert sample lendings
INSERT INTO lendings (user_id, book_id, lending_date, return_date) VALUES
(2, 1, '2025-05-15 10:00:00', null),
(2, 2, '2025-05-20 14:30:00', '2025-06-27 14:30:00'),
(3, 3, '2025-05-25 09:15:00', '2025-06-29 09:15:00'),
(2, 4, '2025-05-30 11:00:00', '2025-07-07 11:00:00'),
(3, 5, '2025-06-01 12:00:00', '2025-07-08 12:00:00'),
(3, 1, '2025-07-25 09:15:00', null);

