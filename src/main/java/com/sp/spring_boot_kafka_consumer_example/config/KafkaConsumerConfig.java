package com.sp.spring_boot_kafka_consumer_example.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private final CustomKafkaProperties customKafkaProperties;

    public KafkaConsumerConfig(CustomKafkaProperties customKafkaProperties) {
        this.customKafkaProperties = customKafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, customKafkaProperties.getBootstrapServers());
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, customKafkaProperties.getGroupId());
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, customKafkaProperties.getKeyDeserialization());
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, customKafkaProperties.getValueDeserialization());
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        kafkaListenerContainerFactory.getContainerProperties().setObservationEnabled(true);
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return kafkaListenerContainerFactory;
    }


}
