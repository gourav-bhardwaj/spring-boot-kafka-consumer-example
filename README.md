# Spring kafka consumer example

## DLQ concept

- DLQ mean DEAD-LETTER-QUEUE which is use to keep the messages, failed to process or consumed.

## DLT in spring kafka consumer

- Similar as DLQ in spring kafka, we have similar concept but with different name, known as DLT (DEAD LETTER TOPIC).
  But before to use it, we need to provide ProducerFactory and KafkaTemplate configuration.

```java
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        return new DefaultKafkaProducerFactory<>(configMap, StringSerializer::new, StringSerializer::new);
    }

    @Bean
    public KafkaTemplate<String, String> retryTopicKafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
```

- Then in listener class on '@KafkaListener' annotated method, We can apply @RetryableTopic with some important attributes.

```java
    @RetryableTopic(attempts = "3",
            kafkaTemplate = "retryTopicKafkaTemplate")
    @KafkaListener(groupId = "${kafka.consumer.group-id}",
            topics = "#{'${kafka.consumer.topics}'.split(',')}",
            containerFactory = "kafkaListenerContainerFactory",
            idIsGroup = true)
    public void myKafkaListener(@Payload String message,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                @Header(KafkaHeaders.OFFSET) long offset,
                                @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long time,
                                Acknowledgment acknowledgment) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        log.info("Topic: {}, Partition: {}, Offset: {}, Timestamp: {}", topic, partition, offset, zonedDateTime);
        log.info("Message to process: {}", message);
        if(message.equalsIgnoreCase("Break")) {
            throw new InvalidReceivedMessage("Dummy exception thrown!!");
        }
        acknowledgementMessage(message, acknowledgment);
    }
```

***Note:*** attempts attribute in @RetryableTopic annotation perform given attempts - 1 retries because considering the initial call as well so here you will see only 2 retry calls.


- If we want to listen that DLT topic and want to take some action like
  - Notify the team to analyze the message content.
  - Customer based on given message details.
  Then in the same class you need to create a method similar as above given listener method and instead of listener you can specify '@DltHandler' annotation over the method.

```java
    @DltHandler
    public void handleDltMessage(@Payload String message,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                @Header(KafkaHeaders.OFFSET) long offset,
                                @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long time,
                                 Acknowledgment acknowledgment) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        String mailTemplate = """
                Hi Team,
                
                    We received a message from topic '%s' but due to some error we unable to process this message.
                    Here are the details given below:
               
                    Dlt Topic: %s
                    Partition: %d
                    Offset: %d
                    Timestamp: %s
               
                    Kindly check this message: %s
                
                Thanks
                Gourav Kumar
                """.formatted(topic.split("-")[0], topic, partition, offset, zonedDateTime, message);
        log.info("Sent email");
        System.out.println(mailTemplate);
        acknowledgementMessage(message, acknowledgment);
    }
```

- Now lets discuss one interesting attributes of @RetryableTopic annotation which is dltStrategy.
  Here you can specify any strategy based on your use case such as
  - ALWAYS_RETRY_ON_ERROR (Default): If i want to do some retry based on given 'attempts' attribute and if still not consumed then send message in DLT.
  - FAIL_ON_ERROR: No retries, If failed to consume the message then directly send into DLT.
  - NO_DLT: Nothing (No retries, No DLT transfer)
    
```java
  public enum DltStrategy {
      NO_DLT,
      ALWAYS_RETRY_ON_ERROR,
      FAIL_ON_ERROR;
  
      private DltStrategy() {
      }
  }
```



