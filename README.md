# InboundAdapter Project: A Versatile Framework for Transactional Messaging

## Abstract

In modern distributed systems, ensuring data consistency across multiple services and technologies is a critical challenge. Two-Phase Commit (2PC) is a widely adopted protocol to guarantee atomicity and consistency in transactions spanning different systems. This paper introduces the InboundAdapter project, a Spring Boot application leveraging Apache Camel to implement transactional messaging with ActiveMQ. The core component, Camel2PC, demonstrates a straightforward yet powerful approach to 2PC implementation, enabling seamless integration with various systems like Kafka and RDS. Our approach emphasizes simplicity, modularity, and extensibility, making it ideal for enterprise-grade applications requiring reliable transactional messaging. The benefits include reduced complexity in managing distributed transactions, enhanced system resilience, and adaptability to evolving business needs. The full code is available on [GitHub](#) for further exploration.

## Introduction

The **InboundAdapter** project is a Spring Boot-based application designed to demonstrate transactional messaging using Apache Camel. It showcases how Camel routes can be configured to support transactional behavior, specifically two-phase commit (2PC), for reliable and consistent messaging.

The centerpiece of this project is the **Camel2PC** class, which provides a transactional route for publishing messages to two separate ActiveMQ queues. This paper explains the project architecture, the implementation of 2PC in Camel2PC, and outlines how the design can be extended to support additional technologies like Kafka and relational databases (RDS).

The full code is available on [GitHub](#) for reference and further exploration.

---

## Architecture Overview

### Components

1. **Spring Boot Application**:
   - Acts as the host for the Apache Camel routes.
   - Provides configuration and dependency injection.

2. **ActiveMQ Broker**:
   - Serves as the message broker for the transactional queues (`msgQueue` and `idQueue`).

3. **Camel2PC Class**:
   - Defines the transactional route.
   - Manages message publishing to multiple queues within a single transaction.

4. **Transaction Manager**:
   - Configured using Spring's `JmsTransactionManager` to ensure transactional integrity.

---

## Code Explanation

### Camel2PC Class

The `Camel2PC` class defines a transactional route using Apache Camel. Below is the key implementation:

```java
@Component
public class Camel2PC {

    private final CamelContext camelContext;
    private final ProducerTemplate producerTemplate;

    public Camel2PC(CamelContext camelContext) throws Exception {
        this.camelContext = camelContext;
        this.producerTemplate = camelContext.createProducerTemplate();

        // Define the route
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .transacted() // Enable transactional context
                        .log("Publishing message: ${body} with msgid: ${header.msgid}")
                        .to("activemq:queue:msgQueue") // Send to msgQueue
                        .process(exchange -> {
                            // Simulate delay or exception for 2PC testing
                            int msgid = exchange.getIn().getHeader("msgid", Integer.class);
                            exchange.getIn().setBody(msgid);
                            // Uncomment to simulate failure
                            // throw new RuntimeException("Simulated failure after msgQueue update");
                        })
                        .to("activemq:queue:idQueue"); // Send to idQueue
            }
        });

        camelContext.start();
    }

    public void processMessage(String message, int msgid) {
        try {
            producerTemplate.sendBodyAndHeader("direct:start", message, "msgid", msgid);
            System.out.println("Message successfully processed.");
        } catch (Exception e) {
            System.err.println("Transaction rolled back due to exception: " + e.getMessage());
        }
    }
}
```

### Key Features of `Camel2PC`

1. **Transactional Behavior**:
   - The `.transacted()` DSL ensures that all steps within the route are part of a single transaction.

2. **2PC Simulation**:
   - Delays and exceptions can be introduced to simulate real-world scenarios where transactions might fail.

3. **ProducerTemplate**:
   - Used to send messages to the Camel route programmatically.

### Configuration for Transactions

#### ActiveMQ Configuration:

```java
@Configuration
public class ActiveMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory("tcp://localhost:61616");
    }
}
```

#### Transaction Manager Configuration:

```java
@Configuration
public class TransactionManagerConfig {

    @Bean
    public PlatformTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new JmsTransactionManager(connectionFactory);
    }
}
```

---

## Extending Camel2PC for Additional Technologies

The `Camel2PC` class can be easily extended to support 2PC across other technologies like **Kafka** and **RDS**. Below is an outline of the required changes:

### 1. **Add Kafka Integration**

#### Changes:
1. Add Kafka dependencies to `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.apache.camel.springboot</groupId>
       <artifactId>camel-kafka-starter</artifactId>
       <version>4.0.0</version>
   </dependency>
   ```

2. Modify the route to include a Kafka topic:
   ```java
   .to("kafka:topicName") // Send message to Kafka
   ```

3. Ensure transactional Kafka configuration in `application.properties`:
   ```properties
   camel.component.kafka.transactional-id=myKafkaTransaction
   camel.component.kafka.allow-manual-commit=true
   ```

### 2. **Add RDS Integration**

#### Changes:
1. Add JDBC dependencies to `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.apache.camel.springboot</groupId>
       <artifactId>camel-jdbc-starter</artifactId>
       <version>4.0.0</version>
   </dependency>
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
       <version>8.0.34</version>
   </dependency>
   ```

2. Update the route to include a JDBC endpoint:
   ```java
   .to("jdbc:dataSource") // Insert data into RDS
   ```

3. Configure a data source and transaction manager:
   ```java
   @Bean
   public DataSource dataSource() {
       HikariDataSource dataSource = new HikariDataSource();
       dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
       dataSource.setUsername("root");
       dataSource.setPassword("password");
       return dataSource;
   }

   @Bean
   public PlatformTransactionManager transactionManager(DataSource dataSource) {
       return new DataSourceTransactionManager(dataSource);
   }
   ```

---

## Conclusion

The **InboundAdapter** project demonstrates how Apache Camel can be used to implement transactional messaging with 2PC. The modular design of the `Camel2PC` class allows for easy integration with other transactional technologies like Kafka and RDS, enabling developers to build robust, distributed systems.

To explore the full code and examples, visit the [GitHub Repository](#).

