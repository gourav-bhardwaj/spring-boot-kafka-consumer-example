package com.sp.spring_boot_kafka_consumer_example.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "kafka.consumer")
public class CustomKafkaProperties {

    private String bootstrapServers;

    private String groupId;

    private String keyDeserialization;

    private String valueDeserialization;

}
