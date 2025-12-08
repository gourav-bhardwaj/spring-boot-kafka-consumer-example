package com.sp.spring_boot_kafka_consumer_example.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KafkaProducerConfigTest {

    @Mock
    private CustomKafkaProperties customKafkaProperties;

    private KafkaProducerConfig kafkaProducerConfig;

    @BeforeEach
    void setUp() {
        kafkaProducerConfig = new KafkaProducerConfig(customKafkaProperties);
    }

    @Test
    void should_create_producer_factory_with_correct_bootstrap_servers() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");

        // When
        ProducerFactory<String, String> producerFactory = kafkaProducerConfig.producerFactory();

        // Then
        assertThat(producerFactory).isNotNull();
        assertThat(producerFactory.getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    }

    @Test
    void should_create_kafka_template_with_observation_enabled() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        ProducerFactory<String, String> producerFactory = kafkaProducerConfig.producerFactory();

        // When
        KafkaTemplate<String, String> kafkaTemplate = kafkaProducerConfig.retryTopicKafkaTemplate(producerFactory);

        // Then
        assertThat(kafkaTemplate).isNotNull();
        assertThat(kafkaTemplate.isObservationEnabled()).isTrue();
    }

    @Test
    void should_use_string_serializers_in_producer_factory() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");

        // When
        ProducerFactory<String, String> producerFactory = kafkaProducerConfig.producerFactory();

        // Then
        assertThat(producerFactory).isNotNull();
        assertThat(producerFactory.getKeySerializerSupplier().get()).isInstanceOf(StringSerializer.class);
        assertThat(producerFactory.getValueSerializerSupplier().get()).isInstanceOf(StringSerializer.class);
    }

    @Test
    void should_create_retry_topic_kafka_template_successfully() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        ProducerFactory<String, String> producerFactory = kafkaProducerConfig.producerFactory();

        // When
        KafkaTemplate<String, String> kafkaTemplate = kafkaProducerConfig.retryTopicKafkaTemplate(producerFactory);

        // Then
        assertThat(kafkaTemplate).isNotNull();
        assertThat(kafkaTemplate.getProducerFactory()).isEqualTo(producerFactory);
    }

    @Test
    void should_enable_observation_in_kafka_template() {
        // Given
        given(customKafkaProperties.getBootstrapServers()).willReturn("localhost:9092");
        ProducerFactory<String, String> producerFactory = kafkaProducerConfig.producerFactory();

        // When
        KafkaTemplate<String, String> kafkaTemplate = kafkaProducerConfig.retryTopicKafkaTemplate(producerFactory);

        // Then
        assertThat(kafkaTemplate.isObservationEnabled()).isTrue();
    }
}
