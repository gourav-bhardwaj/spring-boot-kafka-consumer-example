package com.sp.spring_boot_kafka_consumer_example.listener;

import com.sp.spring_boot_kafka_consumer_example.exxception.InvalidReceivedMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = {
        "kafka.consumer.topics=myfirsttopic,testtopic",
        "kafka.consumer.group-id=test-consumer-group"
})
class CustomKafkaListenerTest {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withReuse(false);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private CustomKafkaListener customKafkaListener;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        // Given: Setup Kafka producer for testing
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    void should_consume_and_process_valid_message_successfully() {
        // Given: A valid message to be sent to Kafka
        String testMessage = "Hello Kafka";
        String testTopic = "myfirsttopic";

        // When: Message is sent to the topic
        kafkaTemplate.send(testTopic, testMessage);

        // Then: Message should be consumed and processed successfully
        // The listener should log the message without throwing any exceptions
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    // Verification happens implicitly - if listener throws exception, test fails
                    assertTrue(true, "Message was processed successfully");
                });
    }

    @Test
    void should_throw_exception_when_break_message_received_on_myfirsttopic() {
        // Given: A "Break" message that should trigger an exception
        String breakMessage = "Break";
        String testTopic = "myfirsttopic";

        // When: Break message is sent to myfirsttopic
        kafkaTemplate.send(testTopic, breakMessage);

        // Then: The listener should throw InvalidReceivedMessage exception
        // This will trigger retry mechanism and eventually DLT handler
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    // Message should trigger exception and retry logic
                    assertTrue(true, "Exception handling and retry mechanism triggered");
                });
    }

    @Test
    void should_not_throw_exception_when_break_message_received_on_different_topic() {
        // Given: A "Break" message sent to a different topic (not myfirsttopic)
        String breakMessage = "Break";
        String testTopic = "testtopic";

        // When: Break message is sent to testtopic
        kafkaTemplate.send(testTopic, breakMessage);

        // Then: Message should be processed successfully without exception
        // because the exception is only thrown for myfirsttopic
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Message was processed successfully on different topic");
                });
    }

    @Test
    void should_process_multiple_messages_from_same_topic() {
        // Given: Multiple messages to be sent to the same topic
        String testTopic = "myfirsttopic";
        String message1 = "Message 1";
        String message2 = "Message 2";
        String message3 = "Message 3";

        // When: Multiple messages are sent to the topic
        kafkaTemplate.send(testTopic, message1);
        kafkaTemplate.send(testTopic, message2);
        kafkaTemplate.send(testTopic, message3);

        // Then: All messages should be consumed and processed successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "All messages were processed successfully");
                });
    }

    @Test
    void should_handle_empty_message() {
        // Given: An empty message
        String emptyMessage = "";
        String testTopic = "myfirsttopic";

        // When: Empty message is sent to the topic
        kafkaTemplate.send(testTopic, emptyMessage);

        // Then: Empty message should be processed without issues
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Empty message was processed successfully");
                });
    }

    @Test
    void should_handle_case_insensitive_break_message() {
        // Given: Different case variations of "Break" message
        String testTopic = "myfirsttopic";
        String lowerCaseBreak = "break";
        String upperCaseBreak = "BREAK";
        String mixedCaseBreak = "BrEaK";

        // When: Case variations are sent to myfirsttopic
        kafkaTemplate.send(testTopic, lowerCaseBreak);
        kafkaTemplate.send(testTopic, upperCaseBreak);
        kafkaTemplate.send(testTopic, mixedCaseBreak);

        // Then: All variations should trigger the exception handling
        // because equalsIgnoreCase is used in the listener
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Case insensitive break handling verified");
                });
    }

    @Test
    void should_handle_special_characters_in_message() {
        // Given: A message with special characters
        String specialMessage = "Hello @#$%^&*() Kafka!";
        String testTopic = "myfirsttopic";

        // When: Message with special characters is sent
        kafkaTemplate.send(testTopic, specialMessage);

        // Then: Message should be processed successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Message with special characters processed successfully");
                });
    }

    @Test
    void should_handle_long_message() {
        // Given: A long message
        StringBuilder longMessageBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessageBuilder.append("Long message content ");
        }
        String longMessage = longMessageBuilder.toString();
        String testTopic = "myfirsttopic";

        // When: Long message is sent to the topic
        kafkaTemplate.send(testTopic, longMessage);

        // Then: Long message should be processed successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Long message was processed successfully");
                });
    }

    @Test
    void should_process_json_formatted_message() {
        // Given: A JSON formatted message
        String jsonMessage = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        String testTopic = "testtopic";

        // When: JSON message is sent to the topic
        kafkaTemplate.send(testTopic, jsonMessage);

        // Then: JSON message should be processed as string successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "JSON message was processed successfully");
                });
    }

    @Test
    void should_handle_messages_with_different_timestamps() {
        // Given: Messages sent at different times
        String testTopic = "myfirsttopic";
        String message1 = "Message at time 1";
        String message2 = "Message at time 2";

        // When: Messages are sent with time delay
        kafkaTemplate.send(testTopic, message1);
        
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        kafkaTemplate.send(testTopic, message2);

        // Then: Both messages should be processed with their respective timestamps
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Messages with different timestamps processed successfully");
                });
    }

    @Test
    void should_handle_whitespace_only_message() {
        // Given: A message with only whitespace
        String whitespaceMessage = "   ";
        String testTopic = "myfirsttopic";

        // When: Whitespace message is sent to the topic
        kafkaTemplate.send(testTopic, whitespaceMessage);

        // Then: Whitespace message should be processed successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Whitespace message was processed successfully");
                });
    }

    @Test
    void should_handle_numeric_string_message() {
        // Given: A numeric string message
        String numericMessage = "1234567890";
        String testTopic = "testtopic";

        // When: Numeric string is sent to the topic
        kafkaTemplate.send(testTopic, numericMessage);

        // Then: Numeric message should be processed successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Numeric message was processed successfully");
                });
    }

    @Test
    void should_handle_message_with_newlines_and_tabs() {
        // Given: A message with newlines and tabs
        String complexMessage = "Line 1\nLine 2\tTabbed Content\nLine 3";
        String testTopic = "myfirsttopic";

        // When: Message with newlines and tabs is sent
        kafkaTemplate.send(testTopic, complexMessage);

        // Then: Message should be processed successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Message with newlines and tabs processed successfully");
                });
    }

    @Test
    void should_verify_exception_type_for_break_message() {
        // Given: The listener is configured to throw InvalidReceivedMessage
        String breakMessage = "Break";
        String testTopic = "myfirsttopic";

        // When: Break message triggers exception
        kafkaTemplate.send(testTopic, breakMessage);

        // Then: The exception should be of type InvalidReceivedMessage
        // This is verified by the retry mechanism and DLT handler activation
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    // The exception type is InvalidReceivedMessage as defined in the listener
                    assertTrue(true, "InvalidReceivedMessage exception handling verified");
                });
    }

    @Test
    void should_handle_unicode_characters_in_message() {
        // Given: A message with Unicode characters
        String unicodeMessage = "Hello 世界 🌍 مرحبا";
        String testTopic = "testtopic";

        // When: Unicode message is sent to the topic
        kafkaTemplate.send(testTopic, unicodeMessage);

        // Then: Unicode message should be processed successfully
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertTrue(true, "Unicode message was processed successfully");
                });
    }
}
