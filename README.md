# Spring kafka consumer example

## DLQ concept

DLQ mean DEAD-LETTER-QUEUE which is use to keep the messages, failed to process or consumed.

## DLT in spring kafka consumer

Similar as DLQ in spring kafka, we have similar concept but with different name, known as DLT (DEAD LETTER TOPIC).

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

Then in listener class on '@KafkaListener' annotated method, We can apply @RetryableTopic with some important attributes.

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

- Note: attempts attribute in @RetryableTopic annotation perform given attempts - 1 retries because considering the initial call as well so here you will see only 2 retry calls.

If we want to listen


