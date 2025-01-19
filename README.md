# User Service - InvestBuddy AI

The **User Service** handles user management for the **InvestBuddy AI** platform. It provides functionality for user registration, authentication, and user management, while integrating with other services through **Kafka** for seamless communication.

---

## 📜 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Usage](#-usage)
---

## ✨ Features

- **User Registration**: Handles the creation of new users.
- **Authentication**: Provides JWT-based login and token validation.
- **User Management**: Allows users to update personal details (firstname,lastname,birthdate,phonenumber, address email, password).
- **Kafka Communication**:
    - Sends events to the **Notification Service** when a new user is created.
    - Sends events to the **KYC Service** to trigger identity verification upon registration.
- **Integration with Discovery Server**: Registers itself for service discovery and communication.

---

## 🏗️ Architecture

The **User Service** is part of the **InvestBuddy AI** microservices ecosystem and communicates with other services using **Kafka**. Key technologies include:

- **Spring Boot**: Backend framework for REST API development.
- **Spring Security**: Manages authentication and authorization using JWT.
- **PostgreSQL**: Database for persisting user data.
- **Kafka**: Message broker for asynchronous communication.
- **Spring Cloud**: Integration with Eureka for service discovery.

---

## ✅ Prerequisites

Ensure the following are installed before setting up the **User Service**:

- **Java 21** or higher
- **Maven 3.8** or higher
- **PostgreSQL** (or access to a PostgreSQL instance)
- **Kafka** (running instance or cluster)
- **Discovery Server** (Eureka)

---

## 🛠️ Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/your-repo/user-service.git
   cd user-service
