package com.challenge.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(
        classes = {
            RetryConfig.class,
            RetryConfigTest.RecoveringComponent.class,
            RetryConfigTest.AlwaysFailingComponent.class
        })
class RetryConfigTest {

    @Autowired
    private RecoveringComponent recoveringComponent;

    @Autowired
    private AlwaysFailingComponent alwaysFailingComponent;

    @Test
    void retryableMethodRetriesUntilSuccess() {
        String result = recoveringComponent.callUnstableOperation();

        assertThat(result).isEqualTo("success");
        assertThat(recoveringComponent.getAttemptCount()).isEqualTo(3);
    }

    @Test
    void retryableMethodGivesUpAfterMaxAttempts() {
        assertThatThrownBy(alwaysFailingComponent::callUnstableOperation).isInstanceOf(IllegalStateException.class);

        assertThat(alwaysFailingComponent.getAttemptCount()).isEqualTo(3);
    }

    @Component
    public static class RecoveringComponent {

        private final AtomicInteger attempts = new AtomicInteger(0);

        @Retryable(retryFor = IllegalStateException.class, maxAttempts = 3, backoff = @Backoff(delay = 10))
        public String callUnstableOperation() {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new IllegalStateException("Simulated transient failure on attempt " + attempt);
            }
            return "success";
        }

        public int getAttemptCount() {
            return attempts.get();
        }
    }

    @Component
    public static class AlwaysFailingComponent {

        private final AtomicInteger attempts = new AtomicInteger(0);

        @Retryable(retryFor = IllegalStateException.class, maxAttempts = 3, backoff = @Backoff(delay = 10))
        public String callUnstableOperation() {
            attempts.incrementAndGet();
            throw new IllegalStateException("Always fails");
        }

        public int getAttemptCount() {
            return attempts.get();
        }
    }
}
