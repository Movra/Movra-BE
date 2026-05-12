package com.example.movra.presentation.notification.web_push;

import com.example.movra.bc.notification.web_push.application.service.QueryWebPushVapidPublicKeyService;
import com.example.movra.bc.notification.web_push.application.service.RegisterWebPushSubscriptionService;
import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushSubscriptionResponse;
import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushVapidPublicKeyResponse;
import com.example.movra.bc.notification.web_push.presentation.WebPushController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WebPushControllerTest {

    @Mock
    private QueryWebPushVapidPublicKeyService queryWebPushVapidPublicKeyService;

    @Mock
    private RegisterWebPushSubscriptionService registerWebPushSubscriptionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(new WebPushController(
                        queryWebPushVapidPublicKeyService,
                        registerWebPushSubscriptionService
                ))
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("query vapid public key returns configured key")
    void queryVapidPublicKey_success() throws Exception {
        given(queryWebPushVapidPublicKeyService.query())
                .willReturn(new WebPushVapidPublicKeyResponse("public-key"));

        mockMvc.perform(get("/web-push/vapid-public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKey").value("public-key"));
    }

    @Test
    @DisplayName("subscribe registers browser push subscription")
    void subscribe_success() throws Exception {
        UUID subscriptionId = UUID.randomUUID();
        given(registerWebPushSubscriptionService.register(any()))
                .willReturn(WebPushSubscriptionResponse.builder()
                        .webPushSubscriptionId(subscriptionId)
                        .endpoint("https://push.example/subscription/1")
                        .contentEncoding("aes128gcm")
                        .createdAt(Instant.parse("2026-04-30T00:00:00Z"))
                        .lastRegisteredAt(Instant.parse("2026-04-30T00:00:00Z"))
                        .build());

        mockMvc.perform(post("/web-push/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endpoint": "https://push.example/subscription/1",
                                  "keys": {
                                    "p256dh": "p256dh-key",
                                    "auth": "auth-key"
                                  },
                                  "userAgent": "Chrome"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.webPushSubscriptionId").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.endpoint").value("https://push.example/subscription/1"))
                .andExpect(jsonPath("$.contentEncoding").value("aes128gcm"));

        then(registerWebPushSubscriptionService).should().register(any());
    }

    @Test
    @DisplayName("subscribe returns 400 when keys are omitted")
    void subscribe_missingKeys_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/web-push/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endpoint": "https://push.example/subscription/1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
