# OrderApp

API para gerenciamento de pedidos, utilizando **Spring Boot 3.4.2**, **PostgreSQL**, **Redis** e **RabbitMQ**.

## 🚀 Como rodar a aplicação

### 📌 **1. Requisitos**
Certifique-se de ter os seguintes serviços instalados:

- [Docker](https://www.docker.com/)
- [Java 17](https://adoptium.net/temurin/releases/?version=17)
- [Maven](https://maven.apache.org/download.cgi)

---

### 📌 **2. Subir os serviços necessários com Docker**
Antes de iniciar a aplicação, suba os serviços **PostgreSQL**, **RabbitMQ** e **Redis** usando os seguintes comandos:

#### **PostgreSQL**
```sh
docker run --name postgres_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=orderapp -p 5432:5432 -d postgres:15
```

#### **Redis**
```sh
docker run --name redis_cache -p 6379:6379 -d redis:latest
```

#### **RabbitMQ**
```sh
docker run --name rabbitmq_server -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

⚠️ **Observação:** O painel de controle do **RabbitMQ** pode ser acessado em [`http://localhost:15672`](http://localhost:15672)  
**Usuário:** `guest` | **Senha:** `guest`

---

### 📌 **3. Configurar a aplicação**
Edite o arquivo **`application.properties`** para se conectar aos serviços no Docker:

```properties
# Configuração do PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/orderapp
spring.datasource.username=admin
spring.datasource.password=admin
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# Configuração do Redis
spring.redis.host=localhost
spring.redis.port=6379

# Configuração do RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

---

### 📌 **4. Rodar a aplicação**
Após subir os serviços, compile e execute a aplicação:

#### **Compilar e empacotar com Maven**
```sh
mvn clean install
```

#### **Rodar a aplicação**
```sh
mvn spring-boot:run
```

Se estiver rodando no **IntelliJ IDEA**, basta executar a classe **`OrderAppApplication.java`**.

---

### 📌 **5. Acessar os serviços**
- **API da aplicação:** [`http://localhost:8080`](http://localhost:8080)
- **Swagger UI:** [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)
- **Banco de Dados PostgreSQL:** `localhost:5432`
- **Redis:** `localhost:6379`
- **RabbitMQ UI:** [`http://localhost:15672`](http://localhost:15672)

---

### 📌 **6. Parar os serviços**
Se precisar parar os containers, use:

```sh
docker stop postgres_db redis_cache rabbitmq_server
docker rm postgres_db redis_cache rabbitmq_server
```