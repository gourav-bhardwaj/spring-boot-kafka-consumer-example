package com.sp.spring_boot_kafka_consumer_example;

import com.sp.spring_boot_kafka_consumer_example.config.TestKafkaConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Import(TestKafkaConfiguration.class)
class SpringBootKafkaConsumerExampleApplicationTests {

	@Container
	static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

	@DynamicPropertySource
	static void kafkaProperties(DynamicPropertyRegistry registry) {
		registry.add("kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
		registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
	}

	@Test
	void contextLoads() {
		assertThat(kafkaContainer.isRunning()).isTrue();
	}

}
