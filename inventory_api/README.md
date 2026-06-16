# Inventory API

Inventory API is a Spring Boot backend for managing inventory items with photo uploads, geolocation, and AI-powered item suggestions. It provides secure REST endpoints, JWT-based authentication, cloud image support via Cloudinary, and AI-assisted form autocompletion using Ollama vision models.

## Getting Started 🚀

These instructions will help you run a local copy of the project for development and testing purposes.

### Prerequisites 📋

Things you need before installing and running the project:

- Java 21
- Maven 3.9+ (or use the included Maven Wrapper)
- MySQL 8+
- Docker & Docker Compose (optional, for MySQL)
- Firebase project configured for JWT validation
- Cloudinary account for image uploads
- Ollama with a vision model (e.g. `llava`)

Example:

```bash
java -version
mvn -version
```

### Installation 🔧

A step-by-step guide to get a development environment running.

1) Clone the repository

```bash
git clone <repository-url>
cd inventory-api
```

2) Configure environment variables

Use one of the following options.

Option A (recommended): IntelliJ IDEA

- Open **Run | Edit Configurations...**
- Select your Spring Boot run configuration
- Add the variables in **Environment variables**
- Save and run the app from IntelliJ

Option B: Terminal session with `export`

`export` makes variables available to commands executed in the same terminal session.

```bash
export DB_URL=jdbc:mysql://localhost:3306/inventories
export DB_USERNAME=root
export DB_PASSWORD=your_password
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_cloudinary_key
export CLOUDINARY_API_SECRET=your_cloudinary_secret
```

3) Start MySQL with Docker (optional)

```bash
docker-compose up -d
```

4) Install and run Ollama

```bash
# https://ollama.com/download
ollama pull llava
# Ollama runs as a background service on port 11434
```

5) Build the project

```bash
./mvnw clean install
```

6) Run the application

```bash
./mvnw spring-boot:run
```

7) Validate the app is running

```bash
curl http://localhost:8080/v3/api-docs
```

You can now open Swagger UI locally at `http://localhost:8080/swagger-ui/index.html`.

## Running the tests ⚙️

Run the automated tests for this system with:

```bash
./mvnw test
```

## AI-Powered Item Suggestions 🤖

The API includes an AI suggestion endpoint that analyzes item photos using Ollama (local LLM) and returns suggested form fields.

### How it works

1. Upload one or more item photos to `POST /api/ai/item-suggestions`
2. Ollama's vision model (`llava`) analyzes the images
3. The API returns suggested `name`, `description`, `year`, and `categoryName`
4. The category is validated against your existing categories in the database

### Configuration

Ollama is configured in `application.properties`:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llava
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.chat.options.num-predict=512
```

Supported image formats: JPEG, PNG, WebP. Maximum file size: 10MB per image.

## Deployment 📦

Deployment notes:

- Configure all required environment variables in the target environment.
- Ensure MySQL database and external providers (Cloudinary, Ollama) are reachable.
- Validate `/v3/api-docs` after each deployment.
- For production, consider using a cloud-hosted LLM instead of Ollama.

## Built With 🛠️

- [Spring Boot](https://spring.io/projects/spring-boot) - Main backend framework
- [Spring Security](https://spring.io/projects/spring-security) - Authentication and authorization (JWT)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - Data access layer
- [MySQL](https://www.mysql.com/) - Relational database
- [Springdoc OpenAPI](https://springdoc.org/) - API documentation and Swagger UI
- [Spring AI](https://spring.io/projects/spring-ai) - AI integration (Ollama)
- [Cloudinary](https://cloudinary.com/) - Cloud image management
- [Firebase](https://firebase.google.com/) - JWT token validation
- [Flyway](https://flywaydb.org/) - Database migrations
- [MapStruct](https://mapstruct.org/) - DTO mapping
- [Lombok](https://projectlombok.org/) - Boilerplate reduction
- [Maven](https://maven.apache.org/) - Dependency and build management

## Authors ✒️

Built with care by Lluis Arjuan.
