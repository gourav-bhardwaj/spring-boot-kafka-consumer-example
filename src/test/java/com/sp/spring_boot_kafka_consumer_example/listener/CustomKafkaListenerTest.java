package com.sp.spring_boot_kafka_consumer_example.listener;

import com.sp.spring_boot_kafka_consumer_example.exxception.InvalidReceivedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomKafkaListener Unit Tests")
class CustomKafkaListenerTest {

    @InjectMocks
    private CustomKafkaListener customKafkaListener;

    @Mock
    private Acknowledgment acknowledgment;

    private static final String TEST_TOPIC = "myfirsttopic";
    private static final String OTHER_TOPIC = "othertopic";
    private static final int TEST_PARTITION = 0;
    private static final long TEST_OFFSET = 100L;
    private static final long TEST_TIMESTAMP = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        reset(acknowledgment);
    }

    @Test
    @DisplayName("Should successfully process a valid message")
    void testMyKafkaListener_SuccessfulProcessing() {
        String message = "Test message";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should throw exception when message is 'Break' and topic is 'myfirsttopic'")
    void testMyKafkaListener_ThrowsExceptionForBreakMessage() {
        String message = "Break";
        InvalidReceivedMessage exception = assertThrows(InvalidReceivedMessage.class, () -> {
            customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        });
        assertEquals("Dummy exception thrown!!", exception.getMessage());
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should throw exception when message is 'break' (case insensitive)")
    void testMyKafkaListener_ThrowsExceptionForBreakMessageCaseInsensitive() {
        String message = "break";
        InvalidReceivedMessage exception = assertThrows(InvalidReceivedMessage.class, () -> {
            customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        });
        assertEquals("Dummy exception thrown!!", exception.getMessage());
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should throw exception for 'BREAK' (uppercase) and topic 'MYFIRSTTOPIC'")
    void testMyKafkaListener_ThrowsExceptionForBreakMessageUpperCase() {
        String message = "BREAK";
        String topic = "MYFIRSTTOPIC";
        InvalidReceivedMessage exception = assertThrows(InvalidReceivedMessage.class, () -> {
            customKafkaListener.myKafkaListener(message, topic, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        });
        assertEquals("Dummy exception thrown!!", exception.getMessage());
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should process 'Break' message when topic is NOT 'myfirsttopic'")
    void testMyKafkaListener_ProcessesBreakMessageOnDifferentTopic() {
        String message = "Break";
        customKafkaListener.myKafkaListener(message, OTHER_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should process normal message when topic is 'myfirsttopic'")
    void testMyKafkaListener_ProcessesNormalMessageOnFirstTopic() {
        String message = "Normal message";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should process message with different partition")
    void testMyKafkaListener_WithDifferentPartition() {
        String message = "Test message";
        int partition = 5;
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, partition, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should process message with different offset")
    void testMyKafkaListener_WithDifferentOffset() {
        String message = "Test message";
        long offset = 999L;
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, offset, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should successfully handle DLT message")
    void testHandleDltMessage_SuccessfulProcessing() {
        String message = "Failed message";
        String dltTopic = "myfirsttopic-dlt";
        customKafkaListener.handleDltMessage(message, dltTopic, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle DLT message with different partition and offset")
    void testHandleDltMessage_WithDifferentPartitionAndOffset() {
        String message = "Failed message";
        String dltTopic = "myfirsttopic-dlt";
        int partition = 3;
        long offset = 500L;
        customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle DLT message from retry topic")
    void testHandleDltMessage_FromRetryTopic() {
        String message = "Failed message after retries";
        String dltTopic = "othertopic-retry-0-dlt";
        customKafkaListener.handleDltMessage(message, dltTopic, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle empty message")
    void testMyKafkaListener_WithEmptyMessage() {
        String message = "";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle null-like string message")
    void testMyKafkaListener_WithNullLikeMessage() {
        String message = "null";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle message with special characters")
    void testMyKafkaListener_WithSpecialCharacters() {
        String message = "Test message with special chars: !@#$%^&*()";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle message with JSON format")
    void testMyKafkaListener_WithJsonMessage() {
        String message = "{\"key\":\"value\",\"number\":123}";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle acknowledgment exception gracefully")
    void testMyKafkaListener_AcknowledgmentException() {
        String message = "Test message";
        doThrow(new RuntimeException("Acknowledgment failed")).when(acknowledgment).acknowledge();
        assertDoesNotThrow(() -> {
            customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        });
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle acknowledgment exception in DLT handler gracefully")
    void testHandleDltMessage_AcknowledgmentException() {
        String message = "Failed message";
        String dltTopic = "myfirsttopic-dlt";
        doThrow(new RuntimeException("Acknowledgment failed")).when(acknowledgment).acknowledge();
        assertDoesNotThrow(() -> {
            customKafkaListener.handleDltMessage(message, dltTopic, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        });
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle very long message")
    void testMyKafkaListener_WithVeryLongMessage() {
        String message = "A".repeat(10000);
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should process message with minimum offset value")
    void testMyKafkaListener_WithMinimumOffset() {
        String message = "Test message";
        long offset = 0L;
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, offset, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should process message with large offset value")
    void testMyKafkaListener_WithLargeOffset() {
        String message = "Test message";
        long offset = Long.MAX_VALUE;
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, offset, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should handle DLT message with complex topic name")
    void testHandleDltMessage_WithComplexTopicName() {
        String message = "Failed message";
        String dltTopic = "my.complex-topic_name-retry-2-dlt";
        customKafkaListener.handleDltMessage(message, dltTopic, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should not throw exception for 'Break' with whitespace")
    void testMyKafkaListener_BreakMessageWithWhitespace() {
        String message = " Break ";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should not throw exception for partial 'Break' match")
    void testMyKafkaListener_PartialBreakMessage() {
        String message = "Breaking news";
        customKafkaListener.myKafkaListener(message, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, TEST_TIMESTAMP, acknowledgment);
        verify(acknowledgment, times(1)).acknowledge();
    }
}
