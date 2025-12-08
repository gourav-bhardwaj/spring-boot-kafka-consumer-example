package com.sp.spring_boot_kafka_consumer_example.exxception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvalidReceivedMessageTest {

    @Test
    void should_create_exception_with_message() {
        // Given
        String errorMessage = "Invalid message received";

        // When
        InvalidReceivedMessage exception = new InvalidReceivedMessage(errorMessage);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
    }

    @Test
    void should_be_runtime_exception() {
        // Given
        String errorMessage = "Test error";

        // When
        InvalidReceivedMessage exception = new InvalidReceivedMessage(errorMessage);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void should_throw_exception_when_created() {
        // Given
        String errorMessage = "Dummy exception thrown!!";

        // When/Then
        assertThatThrownBy(() -> {
            throw new InvalidReceivedMessage(errorMessage);
        })
                .isInstanceOf(InvalidReceivedMessage.class)
                .hasMessage(errorMessage);
    }

    @Test
    void should_maintain_message_content() {
        // Given
        String expectedMessage = "Expected error message";

        // When
        InvalidReceivedMessage exception = new InvalidReceivedMessage(expectedMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        assertThat(exception.getMessage()).contains("Expected");
        assertThat(exception.getMessage()).contains("error");
    }
}
