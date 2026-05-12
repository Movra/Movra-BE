package com.example.movra.presentation.accountability.accountability_relation;

import com.example.movra.bc.accountability.accountability_relation.application.service.CreateAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.DisconnectAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.JoinAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.UpdateVisibilityPolicyService;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityRelationResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.invite.QueryInviteCodeStatusService;
import com.example.movra.bc.accountability.accountability_relation.application.service.invite.ReissueInviteCodeService;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryFriendAccountabilityStatusService;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.presentation.AccountabilityRelationController;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountabilityRelationControllerTest {

    @Mock
    private CreateAccountabilityRelationService createAccountabilityRelationService;

    @Mock
    private JoinAccountabilityRelationService joinAccountabilityRelationService;

    @Mock
    private ReissueInviteCodeService reissueInviteCodeService;

    @Mock
    private QueryInviteCodeStatusService queryInviteCodeStatusService;

    @Mock
    private QueryFriendAccountabilityStatusService queryFriendAccountabilityStatusService;

    @Mock
    private UpdateVisibilityPolicyService updateVisibilityPolicyService;

    @Mock
    private DisconnectAccountabilityRelationService disconnectAccountabilityRelationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        AccountabilityRelationController controller = new AccountabilityRelationController(
                createAccountabilityRelationService,
                joinAccountabilityRelationService,
                reissueInviteCodeService,
                queryInviteCodeStatusService,
                queryFriendAccountabilityStatusService,
                updateVisibilityPolicyService,
                disconnectAccountabilityRelationService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("create creates accountability relation")
    void create_createsRelation() throws Exception {
        mockMvc.perform(post("/accountability-relations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targets\": [\"FOCUS_SESSION\", \"TOP_PICKS\"]}"))
                .andExpect(status().isOk());

        then(createAccountabilityRelationService).should().create(any());
    }

    @Test
    @DisplayName("join joins accountability relation by invite code")
    void join_joinsRelation() throws Exception {
        mockMvc.perform(post("/accountability-relations/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteCode\": \"ABC123\"}"))
                .andExpect(status().isOk());

        then(joinAccountabilityRelationService).should().join(any());
    }

    @Test
    @DisplayName("reissueInviteCode returns new invite code")
    void reissueInviteCode_returnsNewCode() throws Exception {
        given(reissueInviteCodeService.reissue()).willReturn(
                new InviteCodeResponse("NEWCODE1", LocalDateTime.of(2026, 4, 25, 9, 0, 0))
        );

        mockMvc.perform(post("/accountability-relations/invite-code/reissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value("NEWCODE1"))
                .andExpect(jsonPath("$.expiresAt").value("2026-04-25T09:00:00"));
    }

    @Test
    @DisplayName("queryInviteCodeStatus returns invite code status")
    void queryInviteCodeStatus_returnsStatus() throws Exception {
        given(queryInviteCodeStatusService.query()).willReturn(
                new InviteCodeStatusResponse(
                        "CODE123",
                        LocalDateTime.of(2026, 4, 25, 9, 0, 0),
                        false,
                        true,
                        false
                )
        );

        mockMvc.perform(get("/accountability-relations/invite-code/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value("CODE123"))
                .andExpect(jsonPath("$.expired").value(false))
                .andExpect(jsonPath("$.reissuable").value(true))
                .andExpect(jsonPath("$.watcherConnected").value(false));
    }

    @Test
    @DisplayName("queryFriends returns friend accountability status")
    void queryFriends_returnsStatus() throws Exception {
        UUID relationId = UUID.randomUUID();
        UUID subjectUserId = UUID.randomUUID();
        UUID watcherUserId = UUID.randomUUID();
        given(queryFriendAccountabilityStatusService.query()).willReturn(
                FriendAccountabilityStatusResponse.builder()
                        .watchedByFriends(List.of(FriendAccountabilityRelationResponse.builder()
                                .accountabilityRelationId(relationId)
                                .subjectUserId(subjectUserId)
                                .watcherUserId(watcherUserId)
                                .watcherConnected(true)
                                .allowedTargets(Set.of(MonitoringTarget.FOCUS_SESSION))
                                .build()))
                        .watchingFriends(List.of())
                        .build()
        );

        mockMvc.perform(get("/accountability-relations/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watchedByFriends[0].accountabilityRelationId").value(relationId.toString()))
                .andExpect(jsonPath("$.watchedByFriends[0].subjectUserId").value(subjectUserId.toString()))
                .andExpect(jsonPath("$.watchedByFriends[0].watcherUserId").value(watcherUserId.toString()))
                .andExpect(jsonPath("$.watchedByFriends[0].watcherConnected").value(true))
                .andExpect(jsonPath("$.watchingFriends").isArray());
    }

    @Test
    @DisplayName("updateVisibilityPolicy updates allowed targets")
    void updateVisibilityPolicy_updatesTargets() throws Exception {
        UUID relationId = UUID.randomUUID();
        given(updateVisibilityPolicyService.update(any())).willReturn(
                FriendAccountabilityRelationResponse.builder()
                        .accountabilityRelationId(relationId)
                        .subjectUserId(UUID.randomUUID())
                        .watcherUserId(UUID.randomUUID())
                        .watcherConnected(true)
                        .allowedTargets(Set.of(MonitoringTarget.FOCUS_SESSION, MonitoringTarget.TIMETABLE_TASK))
                        .build()
        );

        mockMvc.perform(patch("/accountability-relations/visibility-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targets\": [\"FOCUS_SESSION\", \"TIMETABLE_TASK\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountabilityRelationId").value(relationId.toString()))
                .andExpect(jsonPath("$.allowedTargets").isArray());

        then(updateVisibilityPolicyService).should().update(any());
    }

    @Test
    @DisplayName("removeWatcherFromMyRelation disconnects watcher")
    void removeWatcherFromMyRelation_disconnectsWatcher() throws Exception {
        mockMvc.perform(delete("/accountability-relations/watcher"))
                .andExpect(status().isOk());

        then(disconnectAccountabilityRelationService).should().removeWatcherFromMyRelation();
    }

    @Test
    @DisplayName("stopWatchingFriend disconnects current watcher")
    void stopWatchingFriend_disconnectsWatcher() throws Exception {
        mockMvc.perform(delete("/accountability-relations/watching"))
                .andExpect(status().isOk());

        then(disconnectAccountabilityRelationService).should().stopWatchingFriend();
    }
}
