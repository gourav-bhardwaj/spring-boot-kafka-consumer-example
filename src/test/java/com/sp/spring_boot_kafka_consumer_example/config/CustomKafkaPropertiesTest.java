package com.sp.spring_boot_kafka_consumer_example.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomKafkaPropertiesTest {

    @Test
    void should_create_custom_kafka_properties_with_default_constructor() {
        // When
        CustomKafkaProperties properties = new CustomKafkaProperties();

        // Then
        assertThat(properties).isNotNull();
        assertThat(properties.getBootstrapServers()).isNull();
        assertThat(properties.getGroupId()).isNull();
        assertThat(properties.getKeyDeserialization()).isNull();
        assertThat(properties.getValueDeserialization()).isNull();
    }

    @Test
    void should_create_custom_kafka_properties_with_all_args_constructor() {
        // Given
        String bootstrapServers = "localhost:9092";
        String groupId = "test-group";
        String keyDeserialization = "org.apache.kafka.common.serialization.StringDeserializer";
        String valueDeserialization = "org.apache.kafka.common.serialization.StringDeserializer";

        // When
        CustomKafkaProperties properties = new CustomKafkaProperties(
                bootstrapServers, groupId, keyDeserialization, valueDeserialization);

        // Then
        assertThat(properties).isNotNull();
        assertThat(properties.getBootstrapServers()).isEqualTo(bootstrapServers);
        assertThat(properties.getGroupId()).isEqualTo(groupId);
        assertThat(properties.getKeyDeserialization()).isEqualTo(keyDeserialization);
        assertThat(properties.getValueDeserialization()).isEqualTo(valueDeserialization);
    }

    @Test
    void should_set_and_get_bootstrap_servers() {
        // Given
        CustomKafkaProperties properties = new CustomKafkaProperties();
        String bootstrapServers = "localhost:9092";

        // When
        properties.setBootstrapServers(bootstrapServers);

        // Then
        assertThat(properties.getBootstrapServers()).isEqualTo(bootstrapServers);
    }

    @Test
    void should_set_and_get_group_id() {
        // Given
        CustomKafkaProperties properties = new CustomKafkaProperties();
        String groupId = "test-group";

        // When
        properties.setGroupId(groupId);

        // Then
        assertThat(properties.getGroupId()).isEqualTo(groupId);
    }

    @Test
    void should_set_and_get_key_deserialization() {
        // Given
        CustomKafkaProperties properties = new CustomKafkaProperties();
        String keyDeserialization = "org.apache.kafka.common.serialization.StringDeserializer";

        // When
        properties.setKeyDeserialization(keyDeserialization);

        // Then
        assertThat(properties.getKeyDeserialization()).isEqualTo(keyDeserialization);
    }

    @Test
    void should_set_and_get_value_deserialization() {
        // Given
        CustomKafkaProperties properties = new CustomKafkaProperties();
        String valueDeserialization = "org.apache.kafka.common.serialization.StringDeserializer";

        // When
        properties.setValueDeserialization(valueDeserialization);

        // Then
        assertThat(properties.getValueDeserialization()).isEqualTo(valueDeserialization);
    }

    @Test
    void should_allow_modification_of_all_properties() {
        // Given
        CustomKafkaProperties properties = new CustomKafkaProperties(
                "old-server", "old-group", "old-key-deser", "old-value-deser");

        // When
        properties.setBootstrapServers("new-server");
        properties.setGroupId("new-group");
        properties.setKeyDeserialization("new-key-deser");
        properties.setValueDeserialization("new-value-deser");

        // Then
        assertThat(properties.getBootstrapServers()).isEqualTo("new-server");
        assertThat(properties.getGroupId()).isEqualTo("new-group");
        assertThat(properties.getKeyDeserialization()).isEqualTo("new-key-deser");
        assertThat(properties.getValueDeserialization()).isEqualTo("new-value-deser");
    }
}
