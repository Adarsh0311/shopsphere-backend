# 🛒 ShopSphere Backend

**ShopSphere Backend** is the Java Spring Boot backend powering ShopSphere, a full-stack e-commerce platform. It features a scalable product catalog, order management, Stripe payments, AWS SQS messaging, and cloud deployment. Ready for production with PostgreSQL, robust API endpoints, and seamless cloud integration.

---

## 🌟 Key Features

- **Product Catalog:** Manage products, categories, inventory, and images.
- **Order Management:** End-to-end order workflow, including status and payment updates.
- **Stripe Payments:** Secure, real-time payment integration.
- **Messaging with AWS SQS:** Asynchronous event-driven processing.
- **Cloud Deployment:** AWS Beanstalk (EC2).
- **Comprehensive REST API:** Well-documented endpoints for frontend and integrations.
- **Production-Ready:** PostgreSQL, error handling, and logging.

---

## 🛠️ Technology Stack

- **Language:** Java (Spring Boot)
- **Database:** PostgreSQL
- **Payments:** Stripe
- **Cloud & Messaging:** AWS (SQS, SNS, EC2, RDS.)
- **Frontend:** [ShopSphere Angular Frontend](https://github.com/Adarsh0311/shopsphere-frontend)

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- PostgreSQL instance
- Stripe API keys
- AWS credentials (for cloud deployment)

### Configuration

All environment variables are configured in the `application.properties` file.

1. Copy the sample configuration:
    ```bash
    cp src/main/resources/application.properties.example src/main/resources/application.properties
    ```
2. Edit `application.properties` to set your database, Stripe, and AWS credentials.

### Build & Run

```bash
# Clone the repository
git clone https://github.com/Adarsh0311/shopsphere-backend.git
cd shopsphere-backend

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

---

## 📦 API Documentation

- Swagger UI available at: `http://localhost:8080/swagger-ui/index.html`  
  Explore and test endpoints interactively.

---

## 🧩 Project Structure

```
src/
 ┣ main/
 ┃ ┣ java/com/shopsphere/          # Core backend source code
 ┃ ┣ resources/                    # Configuration files (application.properties)
 ┣ test/                           # Unit and integration tests
```

---


## 🙌 Author

**Adarsh0311**  
- [GitHub Profile](https://github.com/Adarsh0311)
- [ShopSphere Frontend](https://github.com/Adarsh0311/shopsphere-frontend)

---

> Production-ready, scalable, and cloud-native—ShopSphere is your launchpad for modern e-commerce!
