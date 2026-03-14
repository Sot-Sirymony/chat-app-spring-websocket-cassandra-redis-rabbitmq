# Chat Application (eBook Chat)

## Technology stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 8, Spring Boot 1.5, Spring Security, Spring Web, Spring Data JPA, Spring WebSocket, Spring Session |
| **Frontend** | Next.js 14, React 18, TypeScript, Tailwind CSS, SockJS, STOMP.js |
| **Databases** | MySQL 5.7 (relational), Apache Cassandra 3.0 (audit/analytics), Redis 6.2 (session/cache) |
| **Message broker** | RabbitMQ (AMQP + STOMP on port 61613) |
| **Object storage** | MinIO (S3-compatible) |
| **Auth** | JWT (jjwt), form login, Spring Security |
| **Build / run** | Maven, Docker & Docker Compose, Flyway (DB migrations) |
| **Testing** | JUnit, Mockito, Spring Security Test, Testcontainers |
| **Other** | Thymeleaf (server-rendered UI), Guava, Netty, Reactor, MinIO Java SDK |

---

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
# Must run from ebook-chat (where pom.xml is). From repo root:
cd ebook-chat
mvn spring-boot:run

# Or in one line from anywhere:
cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq/ebook-chat" && mvn spring-boot:run

docker-compose -f docker-compose/dependencies.yml up -d mysql redis cassandra rabbitmq-stomp minio

# Re-build jar and run spring boot
cd ebook-chat
mvn clean package -DskipTests

java -jar target/ebook-chat-*.jar


cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq/ebook-chat" && mvn clean package -DskipTests && java -jar target/ebook-chat-*.jar

cd ebook-chat && mvn clean package -DskipTests



cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq"
docker-compose -f docker-compose/dependencies.yml up -d


cd ebook-chat && mvn clean package -DskipTests && cd ..
docker-compose -f docker-compose/dependencies.yml up -d

cd ebook-chat
mvn spring-boot:run

cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq/ebook-chat" && mvn spring-boot:run