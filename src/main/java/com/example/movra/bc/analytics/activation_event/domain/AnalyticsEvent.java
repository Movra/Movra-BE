package com.example.movra.bc.analytics.activation_event.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.domain.exception.InvalidAnalyticsEventException;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.analytics.activation_event.domain.vo.AnalyticsEventId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "tbl_analytics_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalyticsEvent extends AbstractAggregateRoot {

    private static final int MAX_PROPERTY_COUNT = 20;
    private static final int PROPERTY_KEY_MAX_LENGTH = 100;
    private static final int PROPERTY_VALUE_MAX_LENGTH = 1000;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "analytics_event_id"))
    private AnalyticsEventId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private AnalyticsEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tbl_analytics_event_property",
            joinColumns = @JoinColumn(name = "analytics_event_id")
    )
    @MapKeyColumn(name = "property_key", length = PROPERTY_KEY_MAX_LENGTH)
    @Column(name = "property_value", length = PROPERTY_VALUE_MAX_LENGTH)
    private Map<String, String> properties = new HashMap<>();

    public static AnalyticsEvent record(
            UserId userId,
            AnalyticsEventType eventType,
            Instant occurredAt,
            Map<String, String> properties
    ) {
        Map<String, String> normalizedProperties = normalizeProperties(properties);
        validate(userId, eventType, occurredAt, normalizedProperties);

        return AnalyticsEvent.builder()
                .id(AnalyticsEventId.newId())
                .userId(userId)
                .eventType(eventType)
                .occurredAt(occurredAt)
                .properties(normalizedProperties)
                .build();
    }

    public Map<String, String> getProperties() {
        if (properties == null) {
            return Map.of();
        }

        return Collections.unmodifiableMap(properties);
    }

    private static Map<String, String> normalizeProperties(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return new HashMap<>();
        }

        return new HashMap<>(properties);
    }

    private static void validate(
            UserId userId,
            AnalyticsEventType eventType,
            Instant occurredAt,
            Map<String, String> properties
    ) {
        if (userId == null || eventType == null || occurredAt == null || properties == null) {
            throw new InvalidAnalyticsEventException();
        }

        if (properties.size() > MAX_PROPERTY_COUNT) {
            throw new InvalidAnalyticsEventException();
        }

        properties.forEach((key, value) -> {
            if (key == null || key.isBlank() || key.length() > PROPERTY_KEY_MAX_LENGTH) {
                throw new InvalidAnalyticsEventException();
            }

            if (value == null || value.length() > PROPERTY_VALUE_MAX_LENGTH) {
                throw new InvalidAnalyticsEventException();
            }
        });
    }
}
