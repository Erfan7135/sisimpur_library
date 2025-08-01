# Sisimpur Library Management System

A comprehensive library management system built with Spring Boot and PostgreSQL, designed to modernize the traditional pen-and-paper library operations at Sisimpur Library.

## 📖 Overview

Sisimpur Library has been operating traditionally with pen and paper. Due to a spike in users and activity, the management decided to digitize their system. This application provides a complete solution for managing books, authors, users, and circulation activities.

## ✨ Features

### 🔐 Authentication & Authorization

- JWT-based authentication system
- Role-based access control (ADMIN/USER)
- Secure password encryption with BCrypt

### 📚 Book Management

- CRUD operations for books
- Book search and filtering by title, author, genre, year, and availability
- Stock management and lending tracking
- Category-based organization

### 👤 User Management

- User registration and profile management
- Role assignment (Admin/Regular User)
- User activity tracking

### ✍️ Author Management

- Complete author information management
- Author-book relationship tracking
- Biography and metadata storage

### 🔄 Circulation Management

- Book lending and return operations
- Active lending tracking
- Lending history with filtering

### 🎨 Web Interface

- Modern, responsive web UI
- Separate admin and user interfaces
- Real-time book availability status
- Interactive filtering and search

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.4.3
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Gradle
- **Containerization**: Docker & Docker Compose
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **API Testing**: Bruno API Client

## 🏗️ Architecture

```text
src/
├── main/
│   ├── java/com/sisimpur/library/
│   │   ├── config/          # Security & Web configuration
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Custom exceptions & handlers
│   │   ├── interceptor/    # JWT authentication filter
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── service/        # Business logic layer
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── static/         # Web assets (HTML, CSS, JS)
│       └── application.yaml # Configuration
├── test/                   # Unit tests
├── bruno/                  # API documentation
├── db/                     # Database initialization
└── docker-compose.yaml     # Container orchestration
```

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Docker Desktop
- Git

### Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd sisimpur-main
   ```

2. **Start the database**

   ```bash
   docker compose up -d
   ```

   This will start PostgreSQL on port 5433 with pre-populated sample data.

3. **Run the application**

   ```bash
   ./gradlew bootRun
   ```

   Or on Windows:

   ```cmd
   gradlew.bat bootRun
   ```

4. **Access the application**

   - Main Interface: <http://localhost:8080>
   - Health Check: <http://localhost:8080/api/v1/health>

### Default Login Credentials

- **Admin**: `admin@example.com` / `admin123`
- **User**: `alice@example.com` / `admin123`
- **User**: `bob@example.com` / `admin123`

## 📡 API Endpoints

### Authentication

- `POST /api/v1/auth/login` - User login

### Books

- `GET /api/v1/books` - Get all books (with filtering)
- `GET /api/v1/books/{id}` - Get book by ID
- `POST /api/v1/books` - Create book (Admin only)
- `PUT /api/v1/books/{id}` - Update book (Admin only)
- `DELETE /api/v1/books/{id}` - Delete book (Admin only)

### Authors

- `GET /api/v1/authors` - Get all authors
- `GET /api/v1/authors/{id}` - Get author by ID
- `POST /api/v1/authors` - Create author (Admin only)
- `PUT /api/v1/authors/{id}` - Update author (Admin only)
- `DELETE /api/v1/authors/{id}` - Delete author (Admin only)

### Users

- `GET /api/v1/users` - Get all users (Admin only)
- `GET /api/v1/users/{id}` - Get user by ID (Admin only)
- `POST /api/v1/users` - Create user (Admin only)
- `PUT /api/v1/users/{id}` - Update user (Admin only)
- `DELETE /api/v1/users/{id}` - Delete user (Admin only)

### Circulation

- `GET /api/v1/lending` - Get all lendings (Admin only)
- `GET /api/v1/lending/active` - Get active lendings (Admin only)
- `POST /api/v1/lending/borrow` - Borrow books (Admin only)
- `POST /api/v1/lending/return` - Return books (Admin only)

## 🧪 Testing

### API Testing with Bruno

1. Install [Bruno](https://www.usebruno.com/downloads)
2. Open the `bruno/sisimpur-library` collection
3. Test endpoints using the provided examples

### Running Unit Tests

```bash
./gradlew test
```

## 🗄️ Database Schema

### Core Entities

- **Users**: User accounts with roles
- **Authors**: Book authors with biographies
- **Books**: Book catalog with metadata
- **Lendings**: Borrowing transactions

### Sample Data

The system comes pre-populated with:

- 1 Admin user and 2 regular users
- 2 authors (J.K. Rowling, George R.R. Martin)
- 4 books (Harry Potter series, Game of Thrones series)

## 🔧 Configuration

### Environment Variables

- `SERVER_PORT`: Application port (default: 8080)
- `SPRING_PROFILES_ACTIVE`: Active profile (default: dev)

### Database Configuration

- **Host**: localhost:5433
- **Database**: sisimpur
- **Username**: halum
- **Password**: machvaja

### JWT Configuration

- **Secret**: Configurable in application.yaml
- **Expiration**: 30 minutes (1800000 ms)

## 📁 Static Resources

The application includes several admin panels:

- `admin-authors.html` - Author management
- `admin-books.html` - Book management  
- `admin-circulation.html` - Lending management
- `admin-users.html` - User management
- `index.html` - Main user interface

## 🐛 Error Handling

- Global exception handler for consistent error responses
- Input validation with custom error messages
- Detailed logging for debugging
- User-friendly error messages in the UI

## 🔐 Security Features

- JWT token-based authentication
- Password encryption with BCrypt
- Role-based endpoint protection
- CORS configuration for frontend integration
- SQL injection prevention through JPA

## 📈 Future Enhancements

- Book reservation system
- Email notifications for due dates
- Advanced reporting and analytics
- Mobile application
- Barcode scanning integration
- Fine calculation system

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is created for educational purposes as part of the Sisimpur Library modernization initiative.

---

Happy Reading! 📚