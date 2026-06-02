package com.example.movra.bc.accountability.accountability_relation.application.helper;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationAlreadyExistsException;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.InviteCodeGenerationFailedException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import com.example.movra.sharedkernel.persistence.RequiresNewInsertExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * 전역적으로 유일한 초대 코드를 보장하며 감시 관계를 생성/재발급한다.
 * <p>
 * 초대 코드 컬럼에는 unique 제약({@code uk_accountability_relation_invite_code})이 걸려 있다.
 * 코드가 충돌(중복)하면 DB가 거부하므로, 새 코드를 다시 생성해 재시도한다.
 * 각 시도는 {@link RequiresNewInsertExecutor}의 REQUIRES_NEW 트랜잭션에서 실행돼,
 * flush 실패가 호출 측 트랜잭션을 오염시키지 않고 깨끗하게 재시도할 수 있다.
 */
@Component
@RequiredArgsConstructor
public class InviteCodeIssuer {

    private static final int MAX_ATTEMPTS = 5;
    private static final String SUBJECT_CONSTRAINT = "uk_accountability_relation_subject_user_id";
    private static final String INVITE_CODE_CONSTRAINT = "uk_accountability_relation_invite_code";

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final RequiresNewInsertExecutor requiresNewInsertExecutor;
    private final Clock clock;

    /**
     * 새 감시 관계를 생성하면서 유일한 초대 코드를 발급한다.
     * 주체 유저 중복이면 {@link AccountabilityRelationAlreadyExistsException}을 던진다.
     */
    public AccountabilityRelation issueForNewRelation(UserId subjectUserId, VisibilityPolicy visibilityPolicy) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return requiresNewInsertExecutor.executeAndReturn(() ->
                        accountabilityRelationRepository.saveAndFlush(
                                AccountabilityRelation.create(subjectUserId, visibilityPolicy, clock)
                        )
                );
            } catch (DataIntegrityViolationException e) {
                if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e, SUBJECT_CONSTRAINT)) {
                    throw new AccountabilityRelationAlreadyExistsException();
                }
                if (!DataIntegrityViolationUtils.isDuplicateKeyViolation(e, INVITE_CODE_CONSTRAINT)) {
                    throw e;
                }
                // 초대 코드 충돌 → 새 코드로 재시도
            }
        }
        throw new InviteCodeGenerationFailedException();
    }

    /**
     * 기존 감시 관계에 유일한 초대 코드를 재발급한다.
     * 관계가 없으면 {@link AccountabilityRelationNotFoundException}을 던진다.
     */
    public AccountabilityRelation reissueForSubject(UserId subjectUserId) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return requiresNewInsertExecutor.executeAndReturn(() -> {
                    AccountabilityRelation relation = accountabilityRelationRepository.findBySubjectUserId(subjectUserId)
                            .orElseThrow(AccountabilityRelationNotFoundException::new);
                    relation.generateInviteCode(subjectUserId, clock);
                    return accountabilityRelationRepository.saveAndFlush(relation);
                });
            } catch (DataIntegrityViolationException e) {
                if (!DataIntegrityViolationUtils.isDuplicateKeyViolation(e, INVITE_CODE_CONSTRAINT)) {
                    throw e;
                }
                // 초대 코드 충돌 → 새 코드로 재시도
            }
        }
        throw new InviteCodeGenerationFailedException();
    }
}
