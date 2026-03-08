# [Pro Java Clustering and Scalability: Building Real-Time Apps with Spring, Cassandra, Redis, WebSocket and RabbitMQ](http://a.co/49CPIhv)

![Chat Application](/images/ebook-chat-application.png)

![Pro Java Clustering and Scalability](/images/pro_java_clustering_scalability.jpeg)

# Technologies used in this project

- Spring Boot
- Spring Data (JPA / Cassandra / Redis)
- Spring Security
- Spring WebSocket
- Spring Session
- Cassandra
- Redis
- RabbitMQ
- MySQL
- JUnit, Mockito and TestContainers (spin up Docker containers for Integration Tests)
- Thymeleaf, JQuery and Bootstrap
- Apache Maven (Surefire and Failsafe plugins)

# Setting up this project locally

> **Note:**
The fastest way to get this application up and running locally is using **Docker** and **Docker Compose**.  Be sure that you have at least **Docker 1.13.0** and **Docker Compose 1.11.2** installed on your machine.

1. Clone this repository:
```shell
$ git clone https://github.com/jorgeacetozi/ebook-chat-app-spring-websocket-cassandra-redis-rabbitmq.git
```

2. Enter the repository directory:
```shell
$ cd ebook-chat-app-spring-websocket-cassandra-redis-rabbitmq
```

3. Set up the dependencies (Cassandra, Redis, MySQL and RabbitMQ with STOMP support):
```shell
$ docker-compose -f docker-compose/dependencies.yml up
```

4. Download and start the application:
```shell
$ wget https://github.com/jorgeacetozi/ebook-chat-app-spring-websocket-cassandra-redis/releases/download/ebook-chat-1.0.0/ebook-chat-1.0.0.jar && java -jar ebook-chat-1.0.0.jar
```

5. Navigate to `http://localhost:8080` and have fun!

# Login credentials

**Admin (for UI testing):** username **`admin`**, password **`admin`**. Seeded in `ebook-chat/src/main/resources/db/migration/V1__init.sql`.

**Test user (non-admin, ROLE_USER only):** username **`user`**, password **`password`**. Seeded in `V5__add_test_user.sql`. Use this to test behavior without admin privileges (e.g. no access to `/analytics`, limited room creation).

You can also create a new account from the login page.

# Basic Usage

1. Sign in with username **admin** and password **admin**
2. Create a **New Chat Room** and logout
3. Create your private account
4. Sign in with your account credentials
5. Join the chat room
6. Open a new incognito window and create another account
7. Sign in with this another account
8. Join the chat room
9. Send some messages
10. Open the other browser window and see the messages coming
11. Click the username to send private messages
