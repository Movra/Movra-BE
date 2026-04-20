package com.example.movra.bc.accountability.accountability_relation.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityRelationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountabilityRelationRepository extends JpaRepository<AccountabilityRelation, AccountabilityRelationId> {
     Optional<AccountabilityRelation> findBySubjectUserId(UserId subjectUserId);

     Optional<AccountabilityRelation> findByWatcherUserId(UserId watcherUserId);

     Optional<AccountabilityRelation> findBySubjectUserIdAndWatcherUserId(UserId subjectUserId, UserId watcherUserId);

     Optional<AccountabilityRelation> findByInviteCode_Code(String inviteCodeCode);
}
