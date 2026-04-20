package com.example.movra.bc.accountability.accountability_relation.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.InviteCodeExpiredException;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.InviteCodeNotGeneratedException;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.NotSubjectUserException;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.WatcherAlreadyExistsException;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityInviteCode;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityRelationId;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.Clock;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_accountability_relation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountabilityRelation extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "accountability_relation_id"))
    private AccountabilityRelationId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "subject_user_id", nullable = false))
    private UserId subjectUserId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "watcher_user_id"))
    private UserId watcherUserId; //감시자의 UserId | TODO -> NULL 존재

    @Embedded
    private VisibilityPolicy visibilityPolicy;

    @Embedded
    private AccountabilityInviteCode inviteCode;

    public static AccountabilityRelation create(UserId subjectUserId, VisibilityPolicy visibilityPolicy, Clock clock) {
        return AccountabilityRelation.builder()
                .id(AccountabilityRelationId.newId())
                .subjectUserId(subjectUserId)
                .visibilityPolicy(visibilityPolicy)
                .inviteCode(AccountabilityInviteCode.generate(clock))
                .build();
    }

    public AccountabilityInviteCode generateInviteCode(UserId subjectUserId, Clock clock) {
        validateSubjectUser(subjectUserId);
        validateWatcherNotExists();
        this.inviteCode = AccountabilityInviteCode.generate(clock);
        return this.inviteCode;
    }

    public void joinByInviteCode(String inviteCode, UserId watcherUserId, Clock clock) {
        validateWatcherNotExists();
        validateInviteCode(inviteCode, clock);
        this.watcherUserId = watcherUserId;
    }

    public void ensureMonitoringTargetAllowed(MonitoringTarget monitoringTarget) {
        visibilityPolicy.validateAllowed(monitoringTarget);
    }


    private void validateSubjectUser(UserId subjectUserId) {
        if (!this.subjectUserId.equals(subjectUserId)) {
            throw new NotSubjectUserException();
        }
    }

    private void validateWatcherNotExists() {
        if (this.watcherUserId != null) {
            throw new WatcherAlreadyExistsException();
        }
    }

    private void validateInviteCode(String inviteCode, Clock clock) {
        if (this.inviteCode == null || this.inviteCode.code() == null) {
            throw new InviteCodeNotGeneratedException();
        }

        if (this.inviteCode.isExpired(clock)) {
            throw new InviteCodeExpiredException();
        }

        if (!this.inviteCode.code().equals(inviteCode)) {
            throw new InvalidInviteCodeException();
        }
    }
}
