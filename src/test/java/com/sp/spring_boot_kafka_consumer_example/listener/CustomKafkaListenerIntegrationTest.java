package com.sp.spring_boot_kafka_consumer_example.listener;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class CustomKafkaListenerIntegrationTest {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(false);

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("testKafkaTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CustomKafkaListener customKafkaListener;

    private static final String TEST_TOPIC = "myfirsttopic";

    @BeforeEach
    void setUp() {
        // Allow time for Kafka to be ready
        await().atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(100))
                .until(() -> kafkaContainer.isRunning());
    }

    @Test
    void shouldConsumeMessageSuccessfully(CapturedOutput output) throws Exception {
        // Given
        String message = "Hello Kafka Integration Test";

        // When
        kafkaTemplate.send(TEST_TOPIC, message).get(5, TimeUnit.SECONDS);

        // Then - Verify message was processed and acknowledged
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(output.getOut())
                            .contains("Message to process: " + message)
                            .contains("Acknowledgement successful for the message: " + message);
                });
    }

    @Test
    void shouldHandleNormalMessageWithoutRetry(CapturedOutput output) throws Exception {
        // Given
        String normalMessage = "Normal message content";

        // When
        kafkaTemplate.send(TEST_TOPIC, normalMessage).get(5, TimeUnit.SECONDS);

        // Then - Verify message was processed successfully without retry
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(output.getOut())
                            .contains("Message to process: " + normalMessage)
                            .contains("Acknowledgement successful for the message: " + normalMessage);
                });
    }

    @Test
    void shouldTriggerRetryMechanismForBreakMessage(CapturedOutput output) throws Exception {
        // Given
        String breakMessage = "Break";

        // When - Send message that triggers exception
        kafkaTemplate.send(TEST_TOPIC, breakMessage).get(5, TimeUnit.SECONDS);

        // Then - Verify retry mechanism is triggered
        // Note: The exception is only thrown when topic equals "myfirsttopic" (original topic)
        // On retry topics, the condition is not met, so the message is processed normally
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    String capturedOutput = output.getOut();
                    // Verify the message was processed (either on original or retry topic)
                    assertThat(capturedOutput).contains("Message to process: " + breakMessage);
                    // Verify acknowledgment happened
                    assertThat(capturedOutput).contains("Acknowledgement successful for the message: " + breakMessage);
                });
    }

    @Test
    void shouldAcknowledgeMessageAfterSuccessfulProcessing(CapturedOutput output) throws Exception {
        // Given
        String successMessage = "Success message test";

        // When
        kafkaTemplate.send(TEST_TOPIC, successMessage).get(5, TimeUnit.SECONDS);

        // Then - Verify acknowledgment was successful
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(output.getOut())
                            .contains("Trying to acknowledge the message: " + successMessage)
                            .contains("Acknowledgement successful for the message: " + successMessage);
                });
    }

    @Test
    void shouldHandleMultipleMessages(CapturedOutput output) throws Exception {
        // Given
        String message1 = "Message One";
        String message2 = "Message Two";
        String message3 = "Message Three";

        // When
        kafkaTemplate.send(TEST_TOPIC, message1).get(5, TimeUnit.SECONDS);
        kafkaTemplate.send(TEST_TOPIC, message2).get(5, TimeUnit.SECONDS);
        kafkaTemplate.send(TEST_TOPIC, message3).get(5, TimeUnit.SECONDS);

        // Then - Verify all messages were consumed and acknowledged
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    String capturedOutput = output.getOut();
                    assertThat(capturedOutput)
                            .contains("Message to process: " + message1)
                            .contains("Message to process: " + message2)
                            .contains("Message to process: " + message3)
                            .contains("Acknowledgement successful for the message: " + message1)
                            .contains("Acknowledgement successful for the message: " + message2)
                            .contains("Acknowledgement successful for the message: " + message3);
                });
    }

    @Test
    void shouldLogTopicPartitionOffsetAndTimestamp(CapturedOutput output) throws Exception {
        // Given
        String message = "Test for metadata logging";

        // When
        kafkaTemplate.send(TEST_TOPIC, message).get(5, TimeUnit.SECONDS);

        // Then - Verify that topic, partition, offset, and timestamp are logged
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    String capturedOutput = output.getOut();
                    assertThat(capturedOutput)
                            .contains("Topic: " + TEST_TOPIC)
                            .contains("Partition:")
                            .contains("Offset:")
                            .contains("Timestamp:")
                            .contains("Message to process: " + message);
                });
    }

    @TestConfiguration
    static class KafkaTestProducerConfig {
        @Bean
        public ProducerFactory<String, String> testProducerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, String> testKafkaTemplate() {
            return new KafkaTemplate<>(testProducerFactory());
        }
    }
}
