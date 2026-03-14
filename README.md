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
