# Flicks & Friends - Server Implementation Guide

<div align="center" style="width: 100%; border: 1px solid lightgrey; padding: 10px;">
  <img src="assets/NiroLogo.png" alt="NiroLogo" style="max-width: 100%; height: auto;">
</div>

<br>

![Java](https://img.shields.io/badge/Java-17-orange?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6-green?logo=springboot) ![Gradle](https://img.shields.io/badge/Gradle-7.6-blue?logo=gradle) ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-Enabled-blue?logo=githubactions) ![Google Cloud](https://img.shields.io/badge/Deployed%20on-Google%20Cloud-blue?logo=googlecloud) ![H2 Database](https://img.shields.io/badge/H2%20Database-Enabled-blue?logo=h2) ![SonarCloud](https://img.shields.io/badge/SonarCloud-Enabled-orange?logo=sonarcloud) ![JaCoCo](https://img.shields.io/badge/JaCoCo-Test%20Coverage-green?logo=jacoco) ![Apache License](https://img.shields.io/badge/license-Apache_2.0-blue)

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

Further, our tech stack is underpinned by strong security features such as, **OTP 2FA Authentication**, which enhances security with **One-Time Password (OTP) Two-Factor Authentication (2FA)**, ensuring **secure login** and protecting user data from unauthorized access.

Additionally, the **backend has been rigorously tested** using **SonarCloud and JaCoCo**, ensuring code quality and reliability. The test coverage exceeded **75%**, demonstrating a strong commitment to maintainability and robustness.

This combination of technologies ensures an **efficient and maintainable server-side architecture** while supporting **continuous integration and cloud-based deployment**.

---

##### [Back to Top](#table-of-contents)

## High-Level Components

Below are the 3 main components (i.e., functional groupings that have a focused responsibility) of our application's Backend:

### User Management

This component handles all operations related to user accounts, including creating users, retrieving user information, updating details, and managing authentication (login/logout). The central class for this component is <a href="src/main/java/ch/uzh/ifi/hase/soprafs24/service/UserService.java" style="color: blue; text-decoration: underline;">UserService</a>.

#### Key functionalities:

-   **User registration**:
    -   `createUser(User newUser)`: Creates a new user by generating a unique token, setting the initial status, validating the email, checking for duplicate usernames/emails, encoding the password, and sending a welcome email.
-   **User retrieval**:
    -   `getUsers()`: Retrieves all users from the database.
    -   `getUserById(Long userId)`: Retrieves a specific user by their ID.
-   **User updates**:
    -   `updateUser(Long userId, User userData)`: Allows users to modify their profiles, including username, password, birthday, email, and biography.
-   **Profile picture management**:
    -   `uploadProfilePicture(Long userId, MultipartFile file)`: Handles uploading and storing user profile pictures.
-   **Database interactions**:
    -   Managed using the `UserRepository` interface.

### User Authentication

This component verifies user identity and manages sessions, including login, OTP (One-Time Password) verification, and session handling. <a href="src/main/java/ch/uzh/ifi/hase/soprafs24/controller/UserController.java" style="color: blue; text-decoration: underline;">UserController</a> handles login and OTP verification endpoints, while <a href="src/main/java/ch/uzh/ifi/hase/soprafs24/service/OTPService.java" style="color: blue; text-decoration: underline;">OTPService</a> generates and verifies OTPs.

#### Key functionalities:

-   **Login**:
    -   `loginUser(UserPostDTO userPostDTO)`: Handles user login via `UserController`, validating credentials and setting the user's status to online. Also manages failed login attempts and account locking.
-   **Logout**:
    -   `logoutUser(Long userId)`: Sets the user’s status to offline.
-   **OTP management**:
    -   `sendOTP(Map<String, String> payload)`: Sends OTP for user authentication.
    -   `verifyOTP(Map<String, String> payload)`: Verifies OTP during login.
-   **Token authentication**:
    -   `getUserByToken(String token)`: Retrieves a user based on their authentication token.

### Friend and Watchlist Management

This component manages user friendships and movie watchlists. <a href="src/main/java/ch/uzh/ifi/hase/soprafs24/service/UserService.java" style="color: blue; text-decoration: underline;">UserService</a> handles friend requests, accepting/declining them, retrieving friend lists, and managing movie additions/removals from watchlists. <a href="src/main/java/ch/uzh/ifi/hase/soprafs24/controller/UserController.java" style="color: blue; text-decoration: underline;">UserController</a> exposes API endpoints for these functionalities.

#### Key functionalities:

**Watchlist management**:

-   `addMovieToWatchlist(Long userId, String jsonString)`: Adds a movie🍿 to a user's watchlist.
-   `getWatchlist(Long userId)`: Retrieves the user's watchlist.
-   `removeMovieFromWatchlist(Long userId, String movieId)`: Removes a movie from the user's watchlist.

**Friend request management**:

-   `sendFriendRequest(Long targetUserId, Long fromUserId)`: Sends a friend request.
-   `acceptFriendRequest(Long targetUserId, Long fromUserId)`: Accepts a friend request.
-   `declineFriendRequest(Long targetUserId, Long fromUserId)`: Declines a friend request.
-   `getFriendRequests(Long userId)`: Retrieves the list of friend requests for a user.

**Friend list management**:

-   `getFriends(Long userId)`: Retrieves a user's friends list.
-   `removeFriend(Long userId, Long friendId)`: Removes a friend from the user's list.

**Friend status verification**:

-   `areFriends(Long userId, Long otherUserId)`: Checks if two users are friends.

---

##### [Back to Top](#table-of-contents)

## Launch & Deployment

### Commands to Build and Run Locally

#### Clone the repository:

```sh
git clone https://github.com/aimalai/sopra-fs25-group-29-server.git
cd sopra-fs25-group-29-server
```

#### Navigate to the backend directory:

```sh
cd backend
```

#### Build the backend project:

```sh
./gradlew build
```

#### Run the backend application:

```sh
./gradlew bootRun
```

The backend server should now be running at [http://localhost:8080](http://localhost:8080).

### How to Run Tests

#### Navigate to the backend directory:

```sh
cd backend
```

#### Run backend tests:

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
