# 📚 Book Explorer API

A feature-rich GraphQL API built with Spring Boot that allows users to explore books, manage personal favourites, and leave reviews. It leverages the OpenLibrary API for comprehensive book data while securely managing user-specific interactions in a local database using clean architecture principles.

---

## 🚀 Tech Stack

* **Framework:** Spring Boot 3.5.x
* **API Layer:** Spring GraphQL
* **Security:** Stateless JWT Authentication
* **Data Access:** Spring Data JPA / Hibernate
* **Database:** PostgreSQL
* **Caching:** Redis
* **External Integration:** [OpenLibrary API](https://openlibrary.org/developers/api)
* **Tooling:** Lombok, SLF4J

---

## ✨ Features

* **Book Discovery:** Search for books and fetch detailed metadata via OpenLibrary.
* **Stateless Authentication:** Secure endpoints using JWTs intercepted via a custom `WebGraphQlInterceptor` and injected into the GraphQL Context.
* **Favourites Management:** Authenticated users can add or remove books from their personal favourites list.
* **Review System:** Users can rate (1-5 stars) and review books. Includes CRUD operations with strict ownership validation (users can only edit/delete their own reviews).
* **Aggregated Stats:** Dynamically calculates a book's average rating and total review count.
* **Clean Architecture:** Strict separation of concerns between GraphQL Controllers, Transactional Services, and JPA Repositories.

---

## 🛠️ Getting Started

### Prerequisites
* Java 17 or higher
* Maven
* Redis (running locally or via Docker)
* Your preferred relational database (e.g., PostgreSQL, MySQL)

### Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/channakyanarra-nl/Book-Explorer.git](https://github.com/channakyanarra-nl/Book-Explorer.git)
   cd BookExplorer
   ```

2. **Configure environment variables:**
   Update your `src/main/resources/application.yml` (or `.properties`) with your database credentials, Redis configuration, and JWT Secret:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/bookexplorer
       username: your_db_user
       password: your_db_password
   application:
     security:
       jwt:
         secret-key: "your-very-long-and-secure-secret-key-here"
         expiration: 86400000 # 24 hours
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

---

## 🔐 Authentication

This API uses Bearer tokens for authenticated endpoints (Favourites and Reviews).

To test authenticated endpoints in the built-in GraphiQL UI (`http://localhost:8080/graphiql`):
1. Open the **Headers** tab at the bottom left of the GraphiQL interface.
2. Add your JWT token like this:
   ```json
   {
     "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR..."
   }
   ```

---

## 📖 Example GraphQL Operations

### Query: Search Books
```graphql
query {
  searchBooks(title: "The Lord of the Rings", page: 1, limit: 5) {
    books {
      id
      title
      author
    }
  }
}
```

### Query: Get Book Details with Reviews & Favourites
*Note: `isFavourite` resolves based on the provided JWT.*
```graphql
query {
  book(id: "OL27448W") {
    id
    title
    averageRating
    reviewCount
    isFavourite
    reviews {
      id
      rating
      comment
    }
  }
}
```

### Mutation: Add a Favourite
```graphql
mutation {
  addFavourite(bookId: "OL27448W") {
    success
    message
  }
}
```

### Mutation: Add a Review
```graphql
mutation {
  addReview(input: {
    bookId: "OL27448W",
    rating: 5,
    comment: "An absolute masterpiece!"
  }) {
    id
    rating
    comment
  }
}
```

---

## 🏗️ Project Structure

```text
src/main/java/com/nineleaps/BookExplorer/
├── config/         # Security, Interceptors (AuthInterceptor), and Bean configurations
├── controller/     # GraphQL endpoint mappings (@Controller)
├── dto/            # Data Transfer Objects (Inputs/Outputs)
├── entity/         # JPA Entities (User, Favourite, Review)
├── repository/     # Spring Data JPA Repositories
├── service/        # Business logic and external API calls (@Service, @Transactional)
└── BookExplorerApplication.java
```

---

