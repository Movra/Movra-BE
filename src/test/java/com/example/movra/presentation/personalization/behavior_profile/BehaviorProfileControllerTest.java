package com.example.movra.presentation.personalization.behavior_profile;

import com.example.movra.bc.personalization.behavior_profile.application.service.CreateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.QueryBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.UpdateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.presentation.BehaviorProfileController;
import com.example.movra.config.exception.GlobalExceptionHandler;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BehaviorProfileControllerTest {

    @Mock
    private CreateBehaviorProfileService createBehaviorProfileService;

    @Mock
    private QueryBehaviorProfileService queryBehaviorProfileService;

    @Mock
    private UpdateBehaviorProfileService updateBehaviorProfileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        BehaviorProfileController controller = new BehaviorProfileController(
                createBehaviorProfileService,
                queryBehaviorProfileService,
                updateBehaviorProfileService
        );
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("create returns 400 when preferredFocusStartHour is omitted")
    void create_missingPreferredFocusStartHour_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "executionDifficulty": "MEDIUM",
                  "socialPreference": "MEDIUM",
                  "recoveryStyle": "NEEDS_REFLECTION",
                  "preferredFocusEndHour": 18,
                  "coachingMode": "NEUTRAL"
                }
                """;

        mockMvc.perform(post("/behavior-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("preferredFocusStartHour")));

        then(createBehaviorProfileService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("update returns 400 when preferredFocusEndHour is omitted")
    void update_missingPreferredFocusEndHour_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "executionDifficulty": "MEDIUM",
                  "socialPreference": "MEDIUM",
                  "recoveryStyle": "NEEDS_REFLECTION",
                  "preferredFocusStartHour": 9,
                  "coachingMode": "NEUTRAL"
                }
                """;

        mockMvc.perform(put("/behavior-profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("preferredFocusEndHour")));

        then(updateBehaviorProfileService).shouldHaveNoInteractions();
    }
}
