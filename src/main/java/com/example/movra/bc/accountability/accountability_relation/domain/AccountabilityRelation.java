package com.example.movra.bc.accountability.accountability_relation.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityInviteCode;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityRelationId;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_accountability_relation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountabilityRelation {

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

}