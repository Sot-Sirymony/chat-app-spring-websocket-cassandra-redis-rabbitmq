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

3. Set up the dependencies (Cassandra, Redis, MySQL, MinIO and RabbitMQ with STOMP support).

   **Option A – run only dependencies (then start the app with Maven):**
   ```shell
   $ docker-compose -f docker-compose/dependencies.yml up -d mysql redis cassandra rabbitmq-stomp minio
   ```
   Wait until MySQL and others are healthy (about 30–60 seconds), then start the backend:
   ```shell
   $ cd ebook-chat && mvn spring-boot:run
   ```

   **Option B – run full stack (app runs inside Docker):**
   ```shell
   $ docker-compose -f docker-compose/dependencies.yml up
   ```
   (Requires `ebook-chat-1.0.0.jar` in the repo root; the compose file starts the app container.)

4. Download and start the application (if not using Option B):
```shell
$ wget https://github.com/jorgeacetozi/ebook-chat-app-spring-websocket-cassandra-redis/releases/download/ebook-chat-1.0.0/ebook-chat-1.0.0.jar && java -jar ebook-chat-1.0.0.jar
```

5. Navigate to `http://localhost:8080` and have fun!

## Troubleshooting: "Communications link failure" / "Unable to obtain Jdbc connection"

This means **MySQL (or another dependency) is not running or not reachable** when you run `mvn spring-boot:run`.

**Fix:**

1. **Start the dependency containers first** (from the repo root):
   ```shell
   docker-compose -f docker-compose/dependencies.yml up -d mysql redis cassandra rabbitmq-stomp minio
   ```

2. **Wait for MySQL to be ready** (often 30–60 seconds). Check status:
   ```shell
   docker-compose -f docker-compose/dependencies.yml ps
   ```
   Ensure `mysql` shows as **Up** or **Up (healthy)**.

3. **Optional: test that port 3306 is open:**
   ```shell
   nc -zv 127.0.0.1 3306
   ```
   You should see `Connection to 127.0.0.1 port 3306 [tcp/mysql] succeeded!`. If it fails, Docker may not be running or the MySQL container is still starting.

4. **Then** run the app:
   ```shell
   cd ebook-chat && mvn spring-boot:run
   ```

If you don’t use Docker, install and run MySQL (and Redis, Cassandra, RabbitMQ, MinIO) yourself and point `application.yml` (dev profile) at them.

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


# Credential Test
##User 1 
Username: sirymony.sot 
Password: Xmk2pJUj 
 
##user 2 

Username: dalen.phea 
Password: IAhSfRn9 

 
# Docker Compose Down

 docker-compose -f docker-compose/dependencies.yml down
 docker-compose -f docker-compose/presidio.yml down
 
 cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq"
docker-compose -f docker-compose/dependencies.yml down



# Docker Compose UP
docker-compose -f docker-compose/dependencies.yml up

docker-compose -f docker-compose/dependencies.yml up -d mysql redis cassandra rabbitmq-stomp minio

docker compose -f docker-compose/dependencies.yml up -d

# Spring boot Run
cd ebook-chat
mvn spring-boot:run

cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq/ebook-chat" && mvn spring-boot:run

docker-compose -f docker-compose/dependencies.yml up -d mysql redis cassandra rabbitmq-stomp minio

# Re-build jar and run spring boot
cd ebook-chat
mvn clean package -DskipTests

java -jar target/ebook-chat-*.jar


cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq/ebook-chat" && mvn clean package -DskipTests && java -jar target/ebook-chat-*.jar
