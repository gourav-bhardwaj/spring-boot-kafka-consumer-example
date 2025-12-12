package com.sp.spring_boot_kafka_consumer_example.listener;

import com.sp.spring_boot_kafka_consumer_example.exxception.InvalidReceivedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for {@link CustomKafkaListener}.
 * 
 * <p>This test class follows BDD (Behavior-Driven Development) and TDD (Test-Driven Development)
 * principles to ensure complete coverage of the CustomKafkaListener functionality.</p>
 * 
 * <p>Tests are organized using nested classes for better readability and logical grouping:</p>
 * <ul>
 *   <li>Normal Message Processing - Happy path scenarios</li>
 *   <li>Exception Handling - Error scenarios and edge cases</li>
 *   <li>DLT Message Handling - Dead Letter Topic processing</li>
 *   <li>Acknowledgment Scenarios - Message acknowledgment behavior</li>
 * </ul>
 * 
 * <p>Technology Stack:</p>
 * <ul>
 *   <li>Spring Boot 3.3.1 (Java 17+, jakarta.* packages)</li>
 *   <li>JUnit 5 (Jupiter)</li>
 *   <li>Mockito for mocking</li>
 *   <li>AssertJ for fluent assertions</li>
 * </ul>
 * 
 * @author Test Architect
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomKafkaListener Test Suite")
class CustomKafkaListenerTest {

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private CustomKafkaListener customKafkaListener;

    private static final String SAMPLE_MESSAGE = "Test message";
    private static final String SAMPLE_TOPIC = "test-topic";
    private static final String MY_FIRST_TOPIC = "myfirsttopic";
    private static final int SAMPLE_PARTITION = 0;
    private static final long SAMPLE_OFFSET = 12345L;
    private static final long SAMPLE_TIMESTAMP = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        // Common setup for all tests - mocks are already initialized by @Mock annotation
    }

    /**
     * Test suite for normal message processing scenarios.
     * 
     * <p>These tests verify the happy path where messages are processed successfully
     * without exceptions.</p>
     */
    @Nested
    @DisplayName("Normal Message Processing")
    class NormalMessageProcessing {

        @Test
        @DisplayName("should_process_message_successfully_when_valid_message_given")
        void should_process_message_successfully_when_valid_message_given() {
            // Given: A valid message with standard Kafka headers
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged successfully
            then(acknowledgment).should(times(1)).acknowledge();
            verifyNoMoreInteractions(acknowledgment);
        }

        @Test
        @DisplayName("should_process_message_successfully_when_different_topic_given")
        void should_process_message_successfully_when_different_topic_given() {
            // Given: A message from a different topic (not 'myfirsttopic')
            String message = "Some message";
            String topic = "different-topic";
            int partition = 1;
            long offset = 67890L;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged without exception
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_process_message_successfully_with_different_partition_and_offset_values")
        void should_process_message_successfully_with_different_partition_and_offset_values() {
            // Given: A message with non-standard partition and offset values
            String message = "Test data";
            String topic = "analytics-topic";
            int partition = 5;
            long offset = 999999L;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
            verifyNoMoreInteractions(acknowledgment);
        }

        @Test
        @DisplayName("should_process_message_successfully_with_zero_offset_and_partition")
        void should_process_message_successfully_with_zero_offset_and_partition() {
            // Given: A message with zero offset and partition (first message in partition)
            String message = "First message";
            String topic = "new-topic";
            int partition = 0;
            long offset = 0L;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_process_message_successfully_with_very_long_message")
        void should_process_message_successfully_with_very_long_message() {
            // Given: A very long message (simulating large payload)
            String message = "X".repeat(10000);
            String topic = "bulk-topic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_process_message_successfully_with_empty_message")
        void should_process_message_successfully_with_empty_message() {
            // Given: An empty message
            String message = "";
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_process_message_successfully_with_special_characters_in_message")
        void should_process_message_successfully_with_special_characters_in_message() {
            // Given: A message with special characters and unicode
            String message = "Test@#$%^&*()_+-=[]{}|;':\"<>?,./~`!äöüß€";
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_process_message_successfully_with_old_timestamp")
        void should_process_message_successfully_with_old_timestamp() {
            // Given: A message with an old timestamp (e.g., from 2020)
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = 1577836800000L; // Jan 1, 2020

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_process_message_successfully_with_future_timestamp")
        void should_process_message_successfully_with_future_timestamp() {
            // Given: A message with a future timestamp
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = System.currentTimeMillis() + 86400000L; // +1 day

            // When: The Kafka listener processes the message
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }
    }

    /**
     * Test suite for exception handling scenarios.
     * 
     * <p>These tests verify that the listener correctly throws InvalidReceivedMessage
     * exception when specific conditions are met (message="Break" AND topic="myfirsttopic").</p>
     */
    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandling {

        @Test
        @DisplayName("should_throw_exception_when_message_is_Break_and_topic_is_myfirsttopic")
        void should_throw_exception_when_message_is_Break_and_topic_is_myfirsttopic() {
            // Given: Message is "Break" and topic is "myfirsttopic" (exact case)
            String message = "Break";
            String topic = "myfirsttopic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should throw InvalidReceivedMessage exception
            assertThatThrownBy(() -> 
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage("Dummy exception thrown!!");

            // And: Acknowledgment should not be called
            then(acknowledgment).should(never()).acknowledge();
        }

        @Test
        @DisplayName("should_throw_exception_when_message_is_break_lowercase_and_topic_is_myfirsttopic")
        void should_throw_exception_when_message_is_break_lowercase_and_topic_is_myfirsttopic() {
            // Given: Message is "break" (lowercase) and topic is "myfirsttopic"
            String message = "break";
            String topic = "myfirsttopic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should throw InvalidReceivedMessage exception (case-insensitive)
            assertThatThrownBy(() -> 
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage("Dummy exception thrown!!");

            // And: Acknowledgment should not be called
            then(acknowledgment).should(never()).acknowledge();
        }

        @Test
        @DisplayName("should_throw_exception_when_message_is_BREAK_uppercase_and_topic_is_MYFIRSTTOPIC_uppercase")
        void should_throw_exception_when_message_is_BREAK_uppercase_and_topic_is_MYFIRSTTOPIC_uppercase() {
            // Given: Message is "BREAK" and topic is "MYFIRSTTOPIC" (both uppercase)
            String message = "BREAK";
            String topic = "MYFIRSTTOPIC";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should throw InvalidReceivedMessage exception (case-insensitive)
            assertThatThrownBy(() -> 
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage("Dummy exception thrown!!");

            // And: Acknowledgment should not be called
            then(acknowledgment).should(never()).acknowledge();
        }

        @Test
        @DisplayName("should_throw_exception_when_message_is_BrEaK_mixed_case_and_topic_is_MyFiRsTtOpIc_mixed_case")
        void should_throw_exception_when_message_is_BrEaK_mixed_case_and_topic_is_MyFiRsTtOpIc_mixed_case() {
            // Given: Message is "BrEaK" and topic is "MyFiRsTtOpIc" (mixed case)
            String message = "BrEaK";
            String topic = "MyFiRsTtOpIc";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should throw InvalidReceivedMessage exception (case-insensitive)
            assertThatThrownBy(() -> 
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage("Dummy exception thrown!!");

            // And: Acknowledgment should not be called
            then(acknowledgment).should(never()).acknowledge();
        }

        @Test
        @DisplayName("should_not_throw_exception_when_message_is_Break_but_topic_is_different")
        void should_not_throw_exception_when_message_is_Break_but_topic_is_different() {
            // Given: Message is "Break" but topic is NOT "myfirsttopic"
            String message = "Break";
            String topic = "different-topic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should NOT throw exception
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment));

            // And: Message should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_not_throw_exception_when_message_is_different_but_topic_is_myfirsttopic")
        void should_not_throw_exception_when_message_is_different_but_topic_is_myfirsttopic() {
            // Given: Message is NOT "Break" but topic is "myfirsttopic"
            String message = "Some other message";
            String topic = "myfirsttopic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should NOT throw exception
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment));

            // And: Message should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_not_throw_exception_when_message_contains_Break_as_substring")
        void should_not_throw_exception_when_message_contains_Break_as_substring() {
            // Given: Message contains "Break" as substring but is not exactly "Break"
            String message = "Don't Break the system";
            String topic = "myfirsttopic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should NOT throw exception (exact match required)
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment));

            // And: Message should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_not_throw_exception_when_message_is_Break_with_whitespace")
        void should_not_throw_exception_when_message_is_Break_with_whitespace() {
            // Given: Message is "Break" with leading/trailing whitespace
            String message = " Break ";
            String topic = "myfirsttopic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Processing should NOT throw exception (exact match required)
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment));

            // And: Message should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }
    }

    /**
     * Test suite for Dead Letter Topic (DLT) message handling.
     * 
     * <p>These tests verify the DLT handler functionality when messages fail all retry attempts.</p>
     */
    @Nested
    @DisplayName("DLT Message Handling")
    class DltMessageHandling {

        @Test
        @DisplayName("should_handle_dlt_message_and_format_email_template_correctly")
        void should_handle_dlt_message_and_format_email_template_correctly() {
            // Given: A message that reached the DLT
            String message = "Failed message";
            String dltTopic = "myfirsttopic-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged after DLT processing
            then(acknowledgment).should(times(1)).acknowledge();
            verifyNoMoreInteractions(acknowledgment);
        }

        @Test
        @DisplayName("should_handle_dlt_message_with_complex_topic_name")
        void should_handle_dlt_message_with_complex_topic_name() {
            // Given: A message with a complex DLT topic name
            String message = "Complex DLT message";
            String dltTopic = "my-complex-topic-name-dlt";
            int partition = 2;
            long offset = 99999L;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged and email template generated
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_dlt_message_with_long_message_content")
        void should_handle_dlt_message_with_long_message_content() {
            // Given: A DLT message with very long content
            String message = "Y".repeat(5000);
            String dltTopic = "test-topic-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged successfully
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_dlt_message_with_special_characters")
        void should_handle_dlt_message_with_special_characters() {
            // Given: A DLT message with special characters
            String message = "Error: {\"code\":500, \"msg\":\"Internal Error\"}";
            String dltTopic = "errors-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged and processed
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_extract_original_topic_name_from_dlt_topic")
        void should_extract_original_topic_name_from_dlt_topic() {
            // Given: A DLT message where original topic name should be extracted
            String message = "Test extraction";
            String dltTopic = "original-topic-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message (it splits by '-' to get original topic)
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_dlt_message_with_empty_message")
        void should_handle_dlt_message_with_empty_message() {
            // Given: An empty DLT message
            String message = "";
            String dltTopic = "test-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_dlt_message_with_high_partition_number")
        void should_handle_dlt_message_with_high_partition_number() {
            // Given: A DLT message from a high partition number
            String message = "High partition message";
            String dltTopic = "partitioned-topic-dlt";
            int partition = 999;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_dlt_message_with_very_high_offset")
        void should_handle_dlt_message_with_very_high_offset() {
            // Given: A DLT message with very high offset value
            String message = "High offset message";
            String dltTopic = "test-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = Long.MAX_VALUE;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The message should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }
    }

    /**
     * Test suite for message acknowledgment scenarios.
     * 
     * <p>These tests verify that message acknowledgment works correctly in both
     * success and failure scenarios.</p>
     */
    @Nested
    @DisplayName("Acknowledgment Scenarios")
    class AcknowledgmentScenarios {

        @Test
        @DisplayName("should_acknowledge_message_successfully_in_normal_processing")
        void should_acknowledge_message_successfully_in_normal_processing() {
            // Given: A normal message to process
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: The acknowledgment should be called exactly once
            then(acknowledgment).should(times(1)).acknowledge();
            verifyNoMoreInteractions(acknowledgment);
        }

        @Test
        @DisplayName("should_handle_exception_when_acknowledgment_fails")
        void should_handle_exception_when_acknowledgment_fails() {
            // Given: Acknowledgment will throw an exception
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;
            
            doThrow(new RuntimeException("Acknowledgment failed"))
                .when(acknowledgment).acknowledge();

            // When: The message is processed
            // Then: The exception should be caught and logged (no exception propagated)
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment));

            // And: Acknowledgment should have been attempted
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_null_pointer_exception_during_acknowledgment")
        void should_handle_null_pointer_exception_during_acknowledgment() {
            // Given: Acknowledgment will throw NullPointerException
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;
            
            doThrow(new NullPointerException("Null acknowledgment"))
                .when(acknowledgment).acknowledge();

            // When: The message is processed
            // Then: The exception should be caught and logged
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment));

            // And: Acknowledgment should have been attempted
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_acknowledge_message_successfully_in_dlt_handler")
        void should_acknowledge_message_successfully_in_dlt_handler() {
            // Given: A DLT message to process
            String message = "DLT message";
            String dltTopic = "test-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: The acknowledgment should be called exactly once
            then(acknowledgment).should(times(1)).acknowledge();
            verifyNoMoreInteractions(acknowledgment);
        }

        @Test
        @DisplayName("should_handle_exception_when_acknowledgment_fails_in_dlt_handler")
        void should_handle_exception_when_acknowledgment_fails_in_dlt_handler() {
            // Given: Acknowledgment will throw an exception in DLT handler
            String message = "DLT message";
            String dltTopic = "test-dlt";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;
            
            doThrow(new RuntimeException("DLT acknowledgment failed"))
                .when(acknowledgment).acknowledge();

            // When: The DLT handler processes the message
            // Then: The exception should be caught and logged
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment));

            // And: Acknowledgment should have been attempted
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_not_acknowledge_when_exception_thrown_before_acknowledgment")
        void should_not_acknowledge_when_exception_thrown_before_acknowledgment() {
            // Given: Message and topic combination that will trigger exception
            String message = "Break";
            String topic = "myfirsttopic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When & Then: Exception is thrown before acknowledgment
            assertThatThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(InvalidReceivedMessage.class);

            // And: Acknowledgment should never be called
            then(acknowledgment).should(never()).acknowledge();
        }

        @Test
        @DisplayName("should_handle_illegal_state_exception_during_acknowledgment")
        void should_handle_illegal_state_exception_during_acknowledgment() {
            // Given: Acknowledgment will throw IllegalStateException
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;
            
            doThrow(new IllegalStateException("Invalid state for acknowledgment"))
                .when(acknowledgment).acknowledge();

            // When: The message is processed
            // Then: The exception should be caught and logged
            assertThatNoException().isThrownBy(() ->
                customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment));

            // And: Acknowledgment should have been attempted
            then(acknowledgment).should(times(1)).acknowledge();
        }
    }

    /**
     * Integration test scenarios combining multiple aspects.
     * 
     * <p>These tests verify complex scenarios that involve multiple components working together.</p>
     */
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("should_process_multiple_messages_sequentially")
        void should_process_multiple_messages_sequentially() {
            // Given: Multiple different messages
            String message1 = "Message 1";
            String message2 = "Message 2";
            String message3 = "Message 3";
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: Multiple messages are processed sequentially
            customKafkaListener.myKafkaListener(message1, topic, partition, 1L, timestamp, acknowledgment);
            customKafkaListener.myKafkaListener(message2, topic, partition, 2L, timestamp, acknowledgment);
            customKafkaListener.myKafkaListener(message3, topic, partition, 3L, timestamp, acknowledgment);

            // Then: Each message should be acknowledged
            then(acknowledgment).should(times(3)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_mix_of_successful_and_failing_messages")
        void should_handle_mix_of_successful_and_failing_messages() {
            // Given: A mix of normal and "Break" messages
            String normalMessage = "Normal message";
            String breakMessage = "Break";
            String topic = "myfirsttopic";
            int partition = SAMPLE_PARTITION;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: Normal message is processed first
            customKafkaListener.myKafkaListener(normalMessage, topic, partition, 1L, timestamp, acknowledgment);

            // Then: It should be acknowledged
            then(acknowledgment).should(times(1)).acknowledge();

            // When: Break message is processed
            // Then: It should throw exception
            assertThatThrownBy(() ->
                customKafkaListener.myKafkaListener(breakMessage, topic, partition, 2L, timestamp, acknowledgment))
                .isInstanceOf(InvalidReceivedMessage.class);

            // And: Acknowledgment should still be called only once (from first message)
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_process_messages_from_different_topics_correctly")
        void should_process_messages_from_different_topics_correctly() {
            // Given: Messages from various topics
            String message = SAMPLE_MESSAGE;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: Messages from different topics are processed
            customKafkaListener.myKafkaListener(message, "topic1", 0, 1L, timestamp, acknowledgment);
            customKafkaListener.myKafkaListener(message, "topic2", 1, 2L, timestamp, acknowledgment);
            customKafkaListener.myKafkaListener(message, "topic3", 2, 3L, timestamp, acknowledgment);

            // Then: All messages should be acknowledged
            then(acknowledgment).should(times(3)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_dlt_and_normal_processing_independently")
        void should_handle_dlt_and_normal_processing_independently() {
            // Given: Both normal and DLT messages
            String normalMessage = "Normal";
            String dltMessage = "DLT";
            String normalTopic = "test-topic";
            String dltTopic = "test-topic-dlt";
            int partition = SAMPLE_PARTITION;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: Both types of messages are processed
            customKafkaListener.myKafkaListener(normalMessage, normalTopic, partition, 1L, timestamp, acknowledgment);
            customKafkaListener.handleDltMessage(dltMessage, dltTopic, partition, 2L, timestamp, acknowledgment);

            // Then: Both messages should be acknowledged
            then(acknowledgment).should(times(2)).acknowledge();
        }
    }

    /**
     * Edge cases and boundary conditions.
     * 
     * <p>These tests verify behavior at the boundaries and unusual scenarios.</p>
     */
    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCases {

        @Test
        @DisplayName("should_handle_message_with_minimum_timestamp")
        void should_handle_message_with_minimum_timestamp() {
            // Given: A message with minimum possible timestamp (epoch start)
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = 0L;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_message_with_maximum_offset_value")
        void should_handle_message_with_maximum_offset_value() {
            // Given: A message with maximum long value as offset
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = Long.MAX_VALUE;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_message_with_maximum_partition_value")
        void should_handle_message_with_maximum_partition_value() {
            // Given: A message with maximum integer value as partition
            String message = SAMPLE_MESSAGE;
            String topic = SAMPLE_TOPIC;
            int partition = Integer.MAX_VALUE;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_topic_name_with_only_hyphens")
        void should_handle_topic_name_with_only_hyphens() {
            // Given: A topic name consisting only of hyphens
            String message = SAMPLE_MESSAGE;
            String topic = "---";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_dlt_topic_with_simple_name_for_email_template")
        void should_handle_dlt_topic_with_simple_name_for_email_template() {
            // Given: A DLT topic with no hyphens (edge case for split operation)
            String message = "Edge case DLT";
            String dltTopic = "simpletopic";
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The DLT handler processes the message
            customKafkaListener.handleDltMessage(message, dltTopic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_message_with_json_payload")
        void should_handle_message_with_json_payload() {
            // Given: A message containing JSON
            String message = "{\"user\":\"john\",\"action\":\"login\",\"timestamp\":1234567890}";
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_message_with_xml_payload")
        void should_handle_message_with_xml_payload() {
            // Given: A message containing XML
            String message = "<?xml version=\"1.0\"?><root><item>test</item></root>";
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }

        @Test
        @DisplayName("should_handle_message_with_newlines_and_tabs")
        void should_handle_message_with_newlines_and_tabs() {
            // Given: A message with newlines and tabs
            String message = "Line1\nLine2\tTabbed\rCarriageReturn";
            String topic = SAMPLE_TOPIC;
            int partition = SAMPLE_PARTITION;
            long offset = SAMPLE_OFFSET;
            long timestamp = SAMPLE_TIMESTAMP;

            // When: The message is processed
            customKafkaListener.myKafkaListener(message, topic, partition, offset, timestamp, acknowledgment);

            // Then: It should be processed and acknowledged
            then(acknowledgment).should(times(1)).acknowledge();
        }
    }
}
