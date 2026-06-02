package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.helper.InviteCodeIssuer;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationAlreadyExistsException;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.InviteCodeGenerationFailedException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.sharedkernel.persistence.RequiresNewInsertExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class InviteCodeIssuerTest {

    @Mock
    private AccountabilityRelationRepository accountabilityRelationRepository;

    @Mock
    private RequiresNewInsertExecutor requiresNewInsertExecutor;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId userId = UserId.newId();

    private InviteCodeIssuer inviteCodeIssuer;

    @BeforeEach
    void setUp() {
        inviteCodeIssuer = new InviteCodeIssuer(
                accountabilityRelationRepository,
                requiresNewInsertExecutor,
                clock
        );
        // REQUIRES_NEW 실행기는 전달받은 작업을 그대로 실행한다고 가정한다.
        given(requiresNewInsertExecutor.executeAndReturn(any()))
                .willAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    @DisplayName("issueForNewRelation retries with a new code when the invite code collides, then succeeds")
    void issueForNewRelation_retriesOnInviteCodeCollision_thenSucceeds() {
        AccountabilityRelation saved = relation();
        given(accountabilityRelationRepository.saveAndFlush(any(AccountabilityRelation.class)))
                .willThrow(inviteCodeDuplicate())
                .willReturn(saved);

        AccountabilityRelation result = inviteCodeIssuer.issueForNewRelation(userId, policy());

        assertThat(result).isSameAs(saved);
        then(accountabilityRelationRepository).should(times(2)).saveAndFlush(any(AccountabilityRelation.class));
    }

    @Test
    @DisplayName("issueForNewRelation throws AccountabilityRelationAlreadyExistsException on subject-key collision")
    void issueForNewRelation_subjectCollision_throwsAlreadyExists() {
        given(accountabilityRelationRepository.saveAndFlush(any(AccountabilityRelation.class)))
                .willThrow(subjectDuplicate());

        assertThatThrownBy(() -> inviteCodeIssuer.issueForNewRelation(userId, policy()))
                .isInstanceOf(AccountabilityRelationAlreadyExistsException.class);
        then(accountabilityRelationRepository).should(times(1)).saveAndFlush(any(AccountabilityRelation.class));
    }

    @Test
    @DisplayName("issueForNewRelation throws InviteCodeGenerationFailedException when collisions exhaust retries")
    void issueForNewRelation_exhaustsRetries_throwsGenerationFailed() {
        given(accountabilityRelationRepository.saveAndFlush(any(AccountabilityRelation.class)))
                .willThrow(inviteCodeDuplicate());

        assertThatThrownBy(() -> inviteCodeIssuer.issueForNewRelation(userId, policy()))
                .isInstanceOf(InviteCodeGenerationFailedException.class);
        then(accountabilityRelationRepository).should(times(5)).saveAndFlush(any(AccountabilityRelation.class));
    }

    @Test
    @DisplayName("reissueForSubject throws AccountabilityRelationNotFoundException when relation is missing")
    void reissueForSubject_notFound_throwsNotFound() {
        given(accountabilityRelationRepository.findBySubjectUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inviteCodeIssuer.reissueForSubject(userId))
                .isInstanceOf(AccountabilityRelationNotFoundException.class);
        then(accountabilityRelationRepository).should(never()).saveAndFlush(any(AccountabilityRelation.class));
    }

    private AccountabilityRelation relation() {
        return AccountabilityRelation.create(userId, policy(), clock);
    }

    private VisibilityPolicy policy() {
        return new VisibilityPolicy(Set.of(MonitoringTarget.FOCUS_SESSION));
    }

    private DataIntegrityViolationException inviteCodeDuplicate() {
        return new DataIntegrityViolationException(
                "Duplicate entry 'abcd' for key 'tbl_accountability_relation.uk_accountability_relation_invite_code'",
                new SQLException("Duplicate entry", "23000", 1062)
        );
    }

    private DataIntegrityViolationException subjectDuplicate() {
        return new DataIntegrityViolationException(
                "Duplicate entry 'x' for key 'tbl_accountability_relation.uk_accountability_relation_subject_user_id'",
                new SQLException("Duplicate entry", "23000", 1062)
        );
    }
}
