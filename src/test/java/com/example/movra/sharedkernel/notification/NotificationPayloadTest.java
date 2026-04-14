package com.example.movra.sharedkernel.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationPayloadTest {

    @Test
    @DisplayName("payload normalizes null data to an empty map")
    void of_nullData_returnsEmptyMap() {
        NotificationPayload payload = NotificationPayload.of(NotificationType.DAILY_FOCUS, "title", "body", null);

        assertThat(payload.data()).isEmpty();
    }

    @Test
    @DisplayName("payload requires a non-null notification type")
    void of_nullType_throws() {
        assertThatThrownBy(() -> NotificationPayload.of(null, "title", "body", Map.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type must not be null");
    }

    @Test
    @DisplayName("payload defensively copies data")
    void of_mutableData_copiesEntries() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");

        NotificationPayload payload = NotificationPayload.of(NotificationType.DAILY_FOCUS, "title", "body", data);
        data.put("key", "changed");

        assertThat(payload.data()).containsEntry("key", "value");
        assertThatThrownBy(() -> payload.data().put("another", "entry"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
