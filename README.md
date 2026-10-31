# Streamix – API Gateway

![Build](https://img.shields.io/github/actions/workflow/status/mzilin/streamix-infra-gateway/build.yml?label=Build&logo=github&logoColor=white&style=flat)
![Coverage](https://img.shields.io/codecov/c/github/mzilin/streamix-infra-gateway?label=Coverage&logo=codecov&logoColor=white&style=flat)
![Status](https://img.shields.io/badge/status-complete-brightgreen?label=Status)


This repository contains the **API Gateway** microservice for the **Streamix** (Video Streaming Platform), deployed in the **Infrastructure** cluster. It serves as the entry point for all client requests and coordinates communication between backend services.

For a complete system overview and links to all microservices, please refer to the [Microservices Hub Repository](https://github.com/mzilin/streamix-microservices-hub).


## Table of Contents

* [Introduction](#introduction)
* [Technology Stack](#technology-stack)
* [Dependencies](#dependencies)
* [Setting Up Your Environment](#setting-up-your-environment)
  * [Prerequisites](#prerequisites)
  * [Installation & Running](#installation--running)
  * [Running with Docker](#running-with-docker)
  * [Environment Variables](#environment-variables)
* [Testing](#testing)
* [CI/CD & Deployment](#cicd--deployment)
* [Future Improvements](#future-improvements)
* [License](#license)
* [Contact](#contact)


## Introduction

The **API Gateway** is a core component of the **Video Streaming Platform**. It centralises the management of all client-facing HTTP requests and internally routes them to the appropriate microservices. It handles key responsibilities like:
- **Routing**: Directs requests to specific services based on URL paths and load-balancing strategies.
- **Security**: Enforces authentication and authorization with JWT tokens.
- **Rate Limiting**: Protects backend services from abuse and ensures fair usage.

By consolidating these concerns, the API Gateway simplifies client interactions and enhances the maintainability and scalability of the overall platform.


## Technology Stack

This service is built using a modern, cloud-native Java stack, optimised for reactive, scalable microservices:

- **Java** `21`: LTS version with enhanced performance and modern language features.
- **Spring Boot** `4.1.0`: Rapid development framework for standalone, production-ready Java apps.
- **Spring Cloud** `2025.1.2`: Provides essential microservice components like config management, service discovery and API routing.
- **Gradle** `9.5.1`: Powerful build tool with fast incremental builds and powerful dependency management.
- **Docker**: Containerises apps for consistent, portable development and deployment.


## Dependencies

This project relies on a set of key libraries and frameworks that support its core functionality, infrastructure and testing:

- **Spring Boot**
  - **Actuator**: Exposes app health, metrics, and monitoring endpoints.
  - **OpenTelemetry**: Enables distributed tracing, metrics and log export via OTLP.
  - **Webflux**: Supports building reactive REST APIs with Netty under the hood.
  - **Security**: Manages JWT-based authentication and route protection.
  - **Validation**: Provides declarative validation using JSR-380 annotations.

- **Spring Cloud**
  - **Gateway**: Reactive API gateway for routing, filtering and load balancing incoming requests.
  - **Config Client**: Integrates with a centralised Spring Cloud Config Server for dynamic configuration management.
  - **Netflix Eureka Client**: Integrates with the Eureka Server for service registration and discovery.

- **Streamix Platform**
  - **streamix-observability**: Shared observability configuration for logging, tracing and metrics.
  - **streamix-web-commons**: Shared web utilities and common response handling across services.

- **Security**
  - **JJWT** `0.11.5`: Simplifies secure JWT creation, signing, and parsing.

- **Developer Experience**
  - **Lombok**: Reduces boilerplate with annotations for getters, setters, and constructors.
  - **DevTools**: Enables hot reloading for faster development iterations.

- **Testing**
  - **JUnit**: Framework for writing and running unit and integration tests in Java.
  - **Mockito**: Allows mocking and stubbing dependencies to isolate components during testing.

- **Code Quality**
  - **Jacoco**: Generates code coverage reports for ensuring test completeness.


## Setting Up Your Environment

Follow the steps below to set up your local development environment and run the application.


### Prerequisites

Ensure you have the following installed on your machine:
   - [Java JDK 21](https://www.oracle.com/uk/java/technologies/downloads/#java21)
   - [Gradle 9.5.1](https://gradle.org/)
   - [Docker](https://docs.docker.com/get-started/get-docker/)
   - [Docker Compose](https://docs.docker.com/compose/)


### Installation & Running

1. Clone the repository:
    ```bash
    git clone https://github.com/mzilin/streamix-infra-gateway.git
    ```

2. Switch to the `main` branch:
    ```bash
    cd streamix-infra-gateway
    ```

3. Build the project and run tests:
    ```bash
    ./gradlew clean build
    ```

4. Start the service:
    ```bash
    ./gradlew bootRun
    ```

   The service will start on http://localhost:8080 using the Netty web server (default with Spring WebFlux).


### Running with Docker

1. Create ~/.gradle/gradle.properties with your GitHub credentials:
    ```properties
    gpr.user=your_github_username
    gpr.key=your_pat_token
    ```
2. Build the Docker image:
    ```bash
   docker build \
      --secret id=gradle_properties,src=${HOME}/.gradle/gradle.properties \
      -t streamix-infra-gateway:latest .
    ```
3. Run the container:
    ```bash
   docker run --rm --name streamix_gateway -p 8080:8080 streamix-infra-gateway:latest
    ```


### Environment Variables

The following environment variables must be set before running the service:

| Variable               | Description                                  |
|------------------------|----------------------------------------------|
| `ACCESS_TOKEN_SECRET`  | Secret key used to verify JWT access tokens  |
| `AUTH_CLUSTER_KEY`     | API key for the Auth cluster                 |
| `USERS_CLUSTER_KEY`    | API key for the Users cluster                |
| `MEDIA_CLUSTER_KEY`    | API key for the Media cluster                |
| `DISCOVERY_CLUSTER_KEY`| API key for the Discovery cluster            |
| `STREAMING_CLUSTER_KEY`| API key for the Streaming cluster            |
| `BILLING_CLUSTER_KEY`  | API key for the Billing cluster              |


## Testing

This project uses a combination of **unit tests** and **integration tests** to ensure reliability, correctness and maintainability.

To execute all tests, run:
```bash
./gradlew test
```

This setup ensures that changes can be safely verified and that the codebase remains robust, maintainable and well-documented.


## CI/CD & Deployment

This project uses **GitHub Actions** to automate the build, test and deployment processes, ensuring continuous integration and delivery.

- The **Build** workflow runs on every push and pull request to the `main` and `prod` branches. This step verifies that all commits compile successfully and pass unit tests before they are merged, maintaining code quality.
- The **Codecov** workflow runs on pushes to the main branch, where it executes tests, generates a Jacoco coverage report and uploads the results to Codecov.
- Finally, a **Deploy** workflow is triggered when a pull request is merged into the `prod` branch. It builds a Docker image of the microservice, pushes it to the **AWS ECR** container registry and deploys it to the **AWS ECS** environment.

Branch protections and **GitHub Secrets** are used to manage credentials and safeguard the CI/CD pipelines.


## Future Improvements

TBC


## License

This project is private and proprietary. Unauthorised copying, modification, distribution or use of this software, via any medium, is strictly prohibited without explicit written permission from the owner.


## Contact

For any questions or clarifications about the project, please [reach out](https://www.mariuszilinskas.com/contact) to the project owner.


------
###### © 2024–present Marius Zilinskas. All rights reserved.