package com.sp.spring_boot_kafka_consumer_example.listener;

import com.sp.spring_boot_kafka_consumer_example.exxception.InvalidReceivedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class CustomKafkaListener {

    //    @RetryableTopic(attempts = "4",
//            kafkaTemplate = "retryTopicKafkaTemplate",
//            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR, // Both DLT, Retry topic with Delay retry topics
//            backoff = @Backoff(delayExpression = "${kafka.delay.ms}",
//                    multiplierExpression = "${kafka.delay.multiplier}",
//                    maxDelayExpression = "${kafka.delay.max.ms}"))
    @RetryableTopic(attempts = "3",
            kafkaTemplate = "retryTopicKafkaTemplate",
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR // No retry send it into DEAD-LETTER-TOPIC without retries
//            dltStrategy = DltStrategy.NO_DLT // No DLT and retries
    )
    @KafkaListener(groupId = "${kafka.consumer.group-id}",
            topics = "#{'${kafka.consumer.topics}'.split(',')}",
            containerFactory = "kafkaListenerContainerFactory",
            idIsGroup = true)
    public void myKafkaListener(@Payload String message,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                @Header(KafkaHeaders.OFFSET) long offset,
                                @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long time,
                                Acknowledgment acknowledgment) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        log.info("Topic: {}, Partition: {}, Offset: {}, Timestamp: {}", topic, partition, offset, zonedDateTime);
        log.info("Message to process: {}", message);
        if(message.equalsIgnoreCase("Break") && topic.equalsIgnoreCase("myfirsttopic")) {
            throw new InvalidReceivedMessage("Dummy exception thrown!!");
        }
        acknowledgementMessage(message, acknowledgment);
    }

    @DltHandler
    public void handleDltMessage(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long time,
                                 Acknowledgment acknowledgment) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        String mailTemplate = """
                Hi Team,
                
                    We received a message from topic '%s' but due to some error we unable to process this message.
                    Here are the details given below:
               
                    Dlt Topic: %s
                    Partition: %d
                    Offset: %d
                    Timestamp: %s
               
                    Kindly check this message: %s
                
                Thanks
                Gourav Kumar
                """.formatted(topic.split("-")[0], topic, partition, offset, zonedDateTime, message);
        log.info("Sent email");
        System.out.println(mailTemplate);
        acknowledgementMessage(message, acknowledgment);
    }

    private void acknowledgementMessage(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Trying to acknowledge the message: {}", message);
            acknowledgment.acknowledge();
            log.info("Acknowledgement successful for the message: {}", message);
        } catch (Exception ex) {
            log.error("Error while acknowledge the message: ", ex);
        }
    }

}
