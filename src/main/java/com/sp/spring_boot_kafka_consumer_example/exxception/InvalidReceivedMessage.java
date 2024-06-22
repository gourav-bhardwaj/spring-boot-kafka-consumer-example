package com.sp.spring_boot_kafka_consumer_example.exxception;

public class InvalidReceivedMessage extends RuntimeException {
    public InvalidReceivedMessage(String message) {
        super(message);
    }
}
