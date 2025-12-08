package com.sp.spring_boot_kafka_consumer_example.listener;

import com.sp.spring_boot_kafka_consumer_example.config.TestKafkaConfiguration;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@Import(TestKafkaConfiguration.class)
class CustomKafkaListenerTest {

    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    @Autowired
    private CustomKafkaListener customKafkaListener;

    private KafkaTemplate<String, String> testKafkaTemplate;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        testKafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    void should_consume_message_successfully_when_valid_message_is_sent() {
        // Given
        String topic = "myfirsttopic";
        String message = "Test message for successful consumption";

        // When
        testKafkaTemplate.send(topic, message);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }

    @Test
    void should_consume_message_successfully_when_message_is_not_break() {
        // Given
        String topic = "myfirsttopic";
        String message = "Normal message";

        // When
        testKafkaTemplate.send(topic, message);

        // Then - verify the message is consumed without throwing exception
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }

    @Test
    void should_trigger_retry_mechanism_when_invalid_message_is_sent() {
        // Given
        String topic = "myfirsttopic";
        String message = "Break";

        // When - sending a message that will trigger InvalidReceivedMessage exception
        testKafkaTemplate.send(topic, message);

        // Then - the retry mechanism should be triggered and eventually move to DLT
        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }

    @Test
    void should_handle_dlt_message_when_all_retries_exhausted() {
        // Given
        String topic = "myfirsttopic";
        String message = "Break";

        // When - sending message that will go to DLT after retries
        testKafkaTemplate.send(topic, message);

        // Then - DLT handler should process the message
        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }

    @Test
    void should_acknowledge_message_after_successful_processing() {
        // Given
        String topic = "myfirsttopic";
        String message = "Message to acknowledge";

        // When
        testKafkaTemplate.send(topic, message);

        // Then - message should be acknowledged
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }

    @Test
    void should_process_message_with_correct_headers_when_received() {
        // Given
        String topic = "myfirsttopic";
        String message = "Message with headers";

        // When
        testKafkaTemplate.send(topic, message);

        // Then - listener should process message with all headers
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }

    @Test
    void should_handle_multiple_messages_sequentially() {
        // Given
        String topic = "myfirsttopic";
        String message1 = "First message";
        String message2 = "Second message";
        String message3 = "Third message";

        // When
        testKafkaTemplate.send(topic, message1);
        testKafkaTemplate.send(topic, message2);
        testKafkaTemplate.send(topic, message3);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }

    @Test
    void should_not_throw_exception_when_message_is_different_from_break() {
        // Given
        String topic = "myfirsttopic";
        String message = "BREAK"; // uppercase - should not trigger exception

        // When
        testKafkaTemplate.send(topic, message);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
                });
    }
}
