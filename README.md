# Flicks & Friends - Server Implementation Guide

<div align="center" style="width: 100%; border: 1px solid lightgrey; padding: 10px;">
  <img src="assets/NiroLogo.png" alt="NiroLogo" style="max-width: 100%; height: auto;">
</div>

<br>

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6-green?logo=springboot)](https://spring.io/blog/2021/11/19/spring-boot-2-6-is-now-available) [![Gradle](https://img.shields.io/badge/Gradle-7.6-blue?logo=gradle)](https://docs.gradle.org/7.6/userguide/userguide.html) [![SonarCloud](https://img.shields.io/badge/SonarCloud-Enabled-orange?logo=sonarcloud)](https://sonarcloud.io/organizations/aimalai/projects) [![JaCoCo](https://img.shields.io/badge/JaCoCo-Test%20Coverage-green?logo=jacoco)](https://sonarcloud.io/summary/overall?id=aimalai_sopra-fs25-group-29-server&branch=main) [![Apache License](https://img.shields.io/badge/license-Apache_2.0-blue)](https://github.com/aimalai/sopra-fs25-group-29-server/blob/main/LICENSE)

## Table of Contents

-   [Introduction](#introduction)
-   [Technologies Used](#technologies-used)
-   [High-Level Components](#high-level-components)
-   [Launch & Deployment](#launch--deployment)
-   [Roadmap](#roadmap)
-   [Authors & Acknowledgment](#authors--acknowledgment)
-   [License](#license)

## Introduction

Flicks & Friends 🍿 is your go-to platform for discovering, organizing, and enjoying movies and TV shows with friends. Whether you’re searching for new content, creating watch parties, or sharing recommendations, this app makes it easy to stay connected and entertained. Browse trending titles, manage watchlists, rate and review films, and experience synchronized watch parties—all in one place.

Driven by the increasing desire for community engagement and shared entertainment experiences, the app enables users to register, search titles via integrated APIs, rate and review what they watch, and organize virtual watch parties with friends, all from a single platform. Flicks & Friends aims to deliver a user-centric and collaborative experience.

Our primary motivation for choosing to work on this project is the lack of trustworthy recommendations that we see in the market – in contrast, the customers of Flicks & Friends will be able to rely upon the recommendations of their own friends rather than the recommendations of someone they don’t know about.

---

##### [Back to Top](#table-of-contents)

## Technologies Used

This project leverages a robust and efficient **backend technology stack** to ensure seamless development, deployment, and performance.

-   **Java 17**: Provides a modern, high-performance programming language for building scalable server-side applications.
-   **Spring Boot 2.6**: Simplifies backend development with powerful features for dependency management, security, and microservices architecture.
-   **Gradle 7.6**: Streamlines build automation and dependency management, enabling efficient backend project compilation and testing.
-   **GitHub Actions**: Implements continuous integration and deployment (CI/CD) workflows, automating testing and deployments for server-side development.
-   **Google Cloud**: Hosts and deploys the backend, ensuring high availability, scalability, and cloud-based infrastructure reliability.
-   **H2 Database**: Provides an in-memory relational database, ideal for lightweight testing and rapid backend development.

This combination of technologies ensures an **efficient and maintainable server-side architecture** while supporting **continuous integration and cloud-based deployment**.

---

##### [Back to Top](#table-of-contents)

## High-Level Components

The backend is organized into five main components, each responsible for a distinct area of business logic and API communication.

---

### 1. [User Management](./src/main/java/ch/uzh/ifi/hase/soprafs24/controller/UserController.java)  
**Role:** Manages user accounts, friendships, invitations, and account settings.  
**Responsibilities:**  
- Register and authenticate users (with optional OTP)  
- Handle friend requests, acceptance, and listing  
- Retrieve user info and update profile settings  
- Interface with services like `UserService`, `EmailService`, and `OTPService`  
**Main files:**  
- `UserController.java`  
- `UserService.java`  
- `EmailService.java`, `OTPService.java`  
- `User.java`, `Invite.java`

---

### 2. [Movie API and Watchlists](./src/main/java/ch/uzh/ifi/hase/soprafs24/controller/MovieController.java)  
**Role:** Integrates with external APIs (e.g., TMDB) and handles watchlist logic.  
**Responsibilities:**  
- Search movies via TMDB API  
- Add/remove items to/from personal watchlist  
- Aggregate top-rated items from friends  
- Provide trending content  
**Main files:**  
- `MovieController.java`  
- `MovieService.java`

---

### 3. [Watchparty Management](./src/main/java/ch/uzh/ifi/hase/soprafs24/controller/WatchPartyController.java)  
**Role:** Coordinates creation, invitation, and listing of scheduled watchparties.  
**Responsibilities:**  
- Create new parties with video links and metadata  
- Send and manage party invitations  
- Retrieve upcoming or invited watchparties  
**Main files:**  
- `WatchPartyController.java`  
- `WatchPartyService.java`  
- `WatchParty.java`

---

### 4. [Real-time Lobby Synchronization](./src/main/java/ch/uzh/ifi/hase/soprafs24/controller/LobbyController.java)  
**Role:** Enables synchronized playback and state tracking in watchparty lobbies using WebSocket.  
**Responsibilities:**  
- Track who has joined/left the lobby  
- Manage readiness states  
- Broadcast host’s video timestamp for sync  
**Main files:**  
- `LobbyController.java`  
**Technology:** Spring WebSocket (STOMP + SockJS)

---

### 5. [Ratings and Chat System](./src/main/java/ch/uzh/ifi/hase/soprafs24/controller/UserRatingController.java)  
**Role:** Manages user ratings, textual reviews, and chat messages for movies.  
**Responsibilities:**  
- Submit and update user star ratings and reviews  
- Retrieve average ratings and per-user feedback  
- Send and receive chat messages (via `ChatController`)  
**Main files:**  
- `UserRatingController.java`, `ChatController.java`  
- `UserRatingService.java`, `UserRating.java`


##### [Back to Top](#table-of-contents)

## Launch & Deployment

### Commands to Build and Run Locally

#### Clone the repository:

```sh
git clone https://github.com/aimalai/sopra-fs25-group-29-server.git
```

#### Navigate to the server directory:

```sh
cd sopra-fs25-group-29-server
```

#### Build the backend project:

```sh
./gradlew build
```

#### Run the backend application:

```sh
./gradlew bootrun
```

The backend server should now be running at [http://localhost:8080](http://localhost:8080).

### How to Run Tests:

```sh
./gradlew test
```

This will execute all unit and integration tests located in the `src/test/java` directory.  
Test results will be displayed in the console.

### Backend Dependencies

#### TV and Movies API

The application integrates with the **TMDB API** to fetch Movies and TV related data.

More information about the TMDB API key and token can be found in the `application.properties` file.

#### H2 Database (for development/testing):

```properties
server.port=8080
spring.h2.console.enabled=true
spring.h2.console.settings.web-allow-others=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

**Note:** The H2 console is available at [http://localhost:8080/h2-console/](http://localhost:8080/h2-console/) while the server is running.

### Plugins Used

The following plugins have been integrated into the backend project to support development, dependency management, and code quality:

-   **`org.springframework.boot`**  
    Provides Spring Boot support, enabling easy setup and management of the backend application.

-   **`io.spring.dependency-management`**  
    Efficiently manages project dependencies in Spring-based applications, ensuring version compatibility and stability.

-   **`java`**  
    Enables Java language support, allowing the application to be compiled and run as a standard Java project.

-   **`org.sonarqube`**  
    Integrates with SonarQube for automated code quality checks, helping maintain clean, secure, and maintainable code.

---

##### [Back to Top](#table-of-contents)

## Roadmap

New developers looking to contribute to the project could consider implementing the following features:

### 1. Interactive Watchparty Games/Activities

-   **Description:** Integrate simple games or activities into watch parties, such as trivia about the movie being watched or a shared chat-based game.
-   **Technology:** Utilizes **WebSocket connections** for real-time interaction.
-   **Complexity:** **High** – Requires real-time communication, game logic implementation, and UI design for the games.
-   **Value:** **High** – Enhances user engagement and provides a unique, interactive watch party experience 🎉.

### 2. Personalized Movie Recommendations Based on Social Graph

-   **Description:** Instead of generic recommendations, suggest movies based on what a user's friends are watching and rating.
-   **Technology:** Involves **data analysis** of user relationships and ratings.
-   **Complexity:** **High** – Requires designing a recommendation algorithm to analyze social connections and user preferences.
-   **Value:** **High** – Provides highly relevant and personalized movie suggestions, increasing user satisfaction.

### 3. User-Generated Content Integration (Clips/Reactions)

-   **Description:** Allow users to create and share short clips or reaction videos to specific moments in a movie during a watch party.
-   **Technology:** Requires **video processing**, **content storage**, and **synchronization with movie playback**.
-   **Complexity:** **High** – Involves implementing UI elements for clip creation and sharing while ensuring smooth playback.
-   **Value:** **Very High** – Adds a new dimension of user expression and interaction, making watch parties more dynamic and engaging.

---

##### [Back to Top](#table-of-contents)

## Authors & Acknowledgment

### Authors

The following people were the contributors to this project:

-   **Admir Bjelic** - [GitHub: Admir17](https://github.com/Admir17)
-   **Nirojan Ravichandran** - [GitHub: Zec01](https://github.com/Zec01)
-   **Malaiappan Srinivasan** - [GitHub: aimalai](https://github.com/aimalai)
-   **Mohamed Nacer Chabbi** - [GitHub: recan21](https://github.com/recan21)

### Acknowledgment

The authors would like to express their sincere thanks ✨ to **Diyar Taskiran** for his expert guidance throughout the course of this project. His invaluable insights and mentorship were instrumental in shaping our work.

---

##### [Back to Top](#table-of-contents)

## License

This project is licensed under the Apache License. Please check the [LICENSE](LICENSE) file for more details.
