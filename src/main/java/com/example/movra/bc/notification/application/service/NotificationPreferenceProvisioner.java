package com.example.movra.bc.notification.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.repository.NotificationPreferenceRepository;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationPreferenceProvisioner {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    // 홈 조회는 readOnly 트랜잭션이므로 기본값 자동 생성(쓰기)을 REQUIRES_NEW 로 분리한다.
    // 동시 최초 진입 시 unique 위반은 재조회로 복구한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationPreference createOrLoad(UserId userId) {
        try {
            return notificationPreferenceRepository.saveAndFlush(NotificationPreference.createDefault(userId));
        } catch (DataIntegrityViolationException e) {
            if (!DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
                throw e;
            }
            return notificationPreferenceRepository.findByUserId(userId)
                    .orElseThrow(() -> e);
        }
    }
}
