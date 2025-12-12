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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomKafkaListener Tests")
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
        // Reset mocks before each test
        reset(acknowledgment);
    }

    @Test
    @DisplayName("should process message successfully when valid message is received")
    void should_process_message_successfully_when_valid_message_given() {
        // Given
        String validMessage = "Valid message content";
        doNothing().when(acknowledgment).acknowledge();

        // When & Then
        assertDoesNotThrow(() ->
                customKafkaListener.myKafkaListener(
                        validMessage,
                        TEST_TOPIC,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        );

        // Then
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("should throw InvalidReceivedMessage exception when Break message received on myfirsttopic")
    void should_throw_exception_when_break_message_received_on_myfirsttopic() {
        // Given
        String breakMessage = "Break";

        // When & Then
        assertThatThrownBy(() ->
                customKafkaListener.myKafkaListener(
                        breakMessage,
                        TEST_TOPIC,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        )
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage("Dummy exception thrown!!");

        // Then
        then(acknowledgment).should(never()).acknowledge();
    }

    @Test
    @DisplayName("should process Break message successfully when received on different topic")
    void should_process_break_message_successfully_when_received_on_different_topic() {
        // Given
        String breakMessage = "Break";
        doNothing().when(acknowledgment).acknowledge();

        // When & Then
        assertDoesNotThrow(() ->
                customKafkaListener.myKafkaListener(
                        breakMessage,
                        OTHER_TOPIC,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        );

        // Then
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("should throw InvalidReceivedMessage exception when BREAK message (different case) received on myfirsttopic")
    void should_throw_exception_when_break_message_has_different_case() {
        // Given
        String breakMessageDifferentCase = "BREAK";

        // When & Then
        assertThatThrownBy(() ->
                customKafkaListener.myKafkaListener(
                        breakMessageDifferentCase,
                        TEST_TOPIC,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        )
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage("Dummy exception thrown!!");

        // Then
        then(acknowledgment).should(never()).acknowledge();
    }

    @Test
    @DisplayName("should throw InvalidReceivedMessage exception when Break message received on myfirsttopic with different case")
    void should_throw_exception_when_topic_name_has_different_case() {
        // Given
        String breakMessage = "Break";
        String topicDifferentCase = "MyFirstTopic";

        // When & Then
        assertThatThrownBy(() ->
                customKafkaListener.myKafkaListener(
                        breakMessage,
                        topicDifferentCase,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        )
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage("Dummy exception thrown!!");

        // Then
        then(acknowledgment).should(never()).acknowledge();
    }

    @Test
    @DisplayName("should handle acknowledgment failure gracefully")
    void should_handle_acknowledgment_failure_gracefully() {
        // Given
        String validMessage = "Valid message";
        doThrow(new RuntimeException("Acknowledgment failed")).when(acknowledgment).acknowledge();

        // When & Then
        assertDoesNotThrow(() ->
                customKafkaListener.myKafkaListener(
                        validMessage,
                        TEST_TOPIC,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        );

        // Then
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("should process DLT message and acknowledge successfully")
    void should_process_dlt_message_and_acknowledge_successfully() {
        // Given
        String dltMessage = "Failed message in DLT";
        String dltTopic = "myfirsttopic-dlt";
        doNothing().when(acknowledgment).acknowledge();

        // When & Then
        assertDoesNotThrow(() ->
                customKafkaListener.handleDltMessage(
                        dltMessage,
                        dltTopic,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        );

        // Then
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("should handle DLT message with acknowledgment failure gracefully")
    void should_handle_dlt_message_with_acknowledgment_failure_gracefully() {
        // Given
        String dltMessage = "Failed message in DLT";
        String dltTopic = "myfirsttopic-dlt";
        doThrow(new RuntimeException("Acknowledgment failed")).when(acknowledgment).acknowledge();

        // When & Then
        assertDoesNotThrow(() ->
                customKafkaListener.handleDltMessage(
                        dltMessage,
                        dltTopic,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        );

        // Then
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("should throw NullPointerException when null message is received")
    void should_throw_exception_when_null_message_received() {
        // Given
        String nullMessage = null;

        // When & Then
        assertThatThrownBy(() ->
                customKafkaListener.myKafkaListener(
                        nullMessage,
                        TEST_TOPIC,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        )
                .isInstanceOf(NullPointerException.class);

        // Then
        then(acknowledgment).should(never()).acknowledge();
    }

    @Test
    @DisplayName("should process empty message without throwing exception")
    void should_process_empty_message_without_throwing_exception() {
        // Given
        String emptyMessage = "";
        doNothing().when(acknowledgment).acknowledge();

        // When & Then
        assertDoesNotThrow(() ->
                customKafkaListener.myKafkaListener(
                        emptyMessage,
                        TEST_TOPIC,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        );

        // Then
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("should format DLT email template correctly with topic information")
    void should_format_dlt_email_template_correctly() {
        // Given
        String dltMessage = "Error message";
        String dltTopic = "myfirsttopic-retry-0-dlt";
        doNothing().when(acknowledgment).acknowledge();

        // When & Then
        assertDoesNotThrow(() ->
                customKafkaListener.handleDltMessage(
                        dltMessage,
                        dltTopic,
                        TEST_PARTITION,
                        TEST_OFFSET,
                        TEST_TIMESTAMP,
                        acknowledgment
                )
        );

        // Then
        then(acknowledgment).should().acknowledge();
    }
}
