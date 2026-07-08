package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.BehaviorProfileView;

import java.util.Optional;

/**
 * personalization BC(BehaviorProfile) 조회 포트(ACL).
 */
public interface BehaviorProfileReadPort {

    Optional<BehaviorProfileView> findProfile(UserId userId);
}
