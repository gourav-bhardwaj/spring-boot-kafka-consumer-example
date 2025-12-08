package com.sp.spring_boot_kafka_consumer_example.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerConfigTest {

    @Mock
    private CustomKafkaProperties customKafkaProperties;

    private KafkaConsumerConfig kafkaConsumerConfig;

    @BeforeEach
    void setUp() {
        kafkaConsumerConfig = new KafkaConsumerConfig(customKafkaProperties);
    }

    @Test
    void should_create_consumer_factory_with_correct_configuration() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        given(customKafkaProperties.getGroupId()).willReturn("test-group");
        given(customKafkaProperties.getKeyDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");
        given(customKafkaProperties.getValueDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");

        // When
        ConsumerFactory<String, String> consumerFactory = kafkaConsumerConfig.consumerFactory();

        // Then
        assertThat(consumerFactory).isNotNull();
        assertThat(consumerFactory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                .containsEntry(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
                .containsEntry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                .containsEntry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                .containsEntry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    }

    @Test
    void should_create_kafka_listener_container_factory_with_correct_configuration() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        given(customKafkaProperties.getGroupId()).willReturn("test-group");
        given(customKafkaProperties.getKeyDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");
        given(customKafkaProperties.getValueDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");

        ConsumerFactory<String, String> consumerFactory = kafkaConsumerConfig.consumerFactory();

        // When
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                kafkaConsumerConfig.kafkaListenerContainerFactory(consumerFactory);

        // Then
        assertThat(factory).isNotNull();
        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        assertThat(factory.getContainerProperties().isObservationEnabled()).isTrue();
    }

    @Test
    void should_disable_auto_commit_in_consumer_factory() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        given(customKafkaProperties.getGroupId()).willReturn("test-group");
        given(customKafkaProperties.getKeyDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");
        given(customKafkaProperties.getValueDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");

        // When
        ConsumerFactory<String, String> consumerFactory = kafkaConsumerConfig.consumerFactory();

        // Then
        assertThat(consumerFactory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    }

    @Test
    void should_enable_observation_in_listener_container_factory() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        given(customKafkaProperties.getGroupId()).willReturn("test-group");
        given(customKafkaProperties.getKeyDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");
        given(customKafkaProperties.getValueDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");

        ConsumerFactory<String, String> consumerFactory = kafkaConsumerConfig.consumerFactory();

        // When
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                kafkaConsumerConfig.kafkaListenerContainerFactory(consumerFactory);

        // Then
        assertThat(factory.getContainerProperties().isObservationEnabled()).isTrue();
    }

    @Test
    void should_set_manual_immediate_ack_mode_in_listener_container_factory() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        given(customKafkaProperties.getGroupId()).willReturn("test-group");
        given(customKafkaProperties.getKeyDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");
        given(customKafkaProperties.getValueDeserialization()).willReturn("org.apache.kafka.common.serialization.StringDeserializer");

        ConsumerFactory<String, String> consumerFactory = kafkaConsumerConfig.consumerFactory();

        // When
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                kafkaConsumerConfig.kafkaListenerContainerFactory(consumerFactory);

        // Then
        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }
}
