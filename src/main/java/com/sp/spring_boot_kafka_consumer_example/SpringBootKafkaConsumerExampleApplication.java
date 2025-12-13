package com.sp.spring_boot_kafka_consumer_example;

import com.sp.spring_boot_kafka_consumer_example.config.CustomKafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CustomKafkaProperties.class)
public class SpringBootKafkaConsumerExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootKafkaConsumerExampleApplication.class, args);
	}

}
