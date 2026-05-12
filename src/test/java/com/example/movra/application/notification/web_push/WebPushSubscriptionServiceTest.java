package com.example.movra.application.notification.web_push;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.web_push.application.service.QueryWebPushVapidPublicKeyService;
import com.example.movra.bc.notification.web_push.application.service.RegisterWebPushSubscriptionService;
import com.example.movra.bc.notification.web_push.application.service.dto.request.WebPushSubscriptionKeysRequest;
import com.example.movra.bc.notification.web_push.application.service.dto.request.WebPushSubscriptionRequest;
import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushSubscriptionResponse;
import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushVapidPublicKeyResponse;
import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;
import com.example.movra.bc.notification.web_push.domain.exception.InvalidWebPushSubscriptionException;
import com.example.movra.bc.notification.web_push.domain.repository.WebPushSubscriptionRepository;
import com.example.movra.config.webpush.WebPushProperties;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WebPushSubscriptionServiceTest {

    private static final String ENDPOINT = "https://push.example/subscription/1";
    private static final String ENDPOINT_HASH = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Mock
    private WebPushSubscriptionRepository webPushSubscriptionRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    private final UserId userId = UserId.newId();
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-30T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private RegisterWebPushSubscriptionService registerWebPushSubscriptionService;

    @BeforeEach
    void setUp() {
        registerWebPushSubscriptionService = new RegisterWebPushSubscriptionService(
                webPushSubscriptionRepository,
                currentUserQuery,
                analyticsEventRecorder,
                clock
        );
    }

    @Test
    @DisplayName("register creates subscription and records web push opt-in")
    void register_newSubscription_recordsOptIn() {
        givenCurrentUser();
        given(webPushSubscriptionRepository.findByEndpointHash(argThat(hash -> hash.length() == 64)))
                .willReturn(Optional.empty());
        given(webPushSubscriptionRepository.save(any(WebPushSubscription.class)))
                .willAnswer(invocation -> invocation.getArgument(0, WebPushSubscription.class));

        WebPushSubscriptionResponse response = registerWebPushSubscriptionService.register(validRequest());

        assertThat(response.endpoint()).isEqualTo(ENDPOINT);
        assertThat(response.contentEncoding()).isEqualTo("aes128gcm");
        assertThat(response.createdAt()).isEqualTo(clock.instant());
        assertThat(response.lastRegisteredAt()).isEqualTo(clock.instant());
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.WEB_PUSH_OPT_IN),
                argThat(properties ->
                        properties.containsKey("webPushSubscriptionId")
                                && properties.get("contentEncoding").equals("aes128gcm")
                                && properties.get("userAgent").equals("Chrome")
                )
        );
    }

    @Test
    @DisplayName("register refreshes existing subscription without duplicate opt-in")
    void register_existingSameUser_doesNotRecordOptInAgain() {
        givenCurrentUser();
        WebPushSubscription existing = WebPushSubscription.register(
                userId,
                ENDPOINT,
                ENDPOINT_HASH,
                "old-p256dh",
                "old-auth",
                "aes128gcm",
                "Chrome",
                clock
        );
        given(webPushSubscriptionRepository.findByEndpointHash(any())).willReturn(Optional.of(existing));
        given(webPushSubscriptionRepository.save(existing)).willReturn(existing);

        WebPushSubscriptionResponse response = registerWebPushSubscriptionService.register(validRequest());

        assertThat(response.webPushSubscriptionId()).isEqualTo(existing.getId().id());
        assertThat(existing.getP256dhKey()).isEqualTo("p256dh-key");
        assertThat(existing.getAuthKey()).isEqualTo("auth-key");
        then(analyticsEventRecorder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("register throws when request is null")
    void register_nullRequest_throwsException() {
        assertThatThrownBy(() -> registerWebPushSubscriptionService.register(null))
                .isInstanceOf(InvalidWebPushSubscriptionException.class);
    }

    @Test
    @DisplayName("query vapid public key returns configured key")
    void queryVapidPublicKey_success() {
        QueryWebPushVapidPublicKeyService service = new QueryWebPushVapidPublicKeyService(
                new WebPushProperties(false, "public-key", "", "", WebPushProperties.DEFAULT_TTL_SECONDS)
        );

        WebPushVapidPublicKeyResponse response = service.query();

        assertThat(response.publicKey()).isEqualTo("public-key");
    }

    private void givenCurrentUser() {
        given(currentUserQuery.currentUser()).willReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    private WebPushSubscriptionRequest validRequest() {
        return new WebPushSubscriptionRequest(
                ENDPOINT,
                new WebPushSubscriptionKeysRequest("p256dh-key", "auth-key"),
                null,
                "Chrome"
        );
    }
}
