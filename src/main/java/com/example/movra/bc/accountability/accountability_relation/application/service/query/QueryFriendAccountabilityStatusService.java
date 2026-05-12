package com.example.movra.bc.accountability.accountability_relation.application.service.query;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityRelationResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryFriendAccountabilityStatusService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public FriendAccountabilityStatusResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();

        return FriendAccountabilityStatusResponse.builder()
                .watchedByFriends(accountabilityRelationRepository.findAllBySubjectUserId(userId).stream()
                        .map(FriendAccountabilityRelationResponse::from)
                        .toList())
                .watchingFriends(accountabilityRelationRepository.findAllByWatcherUserId(userId).stream()
                        .map(FriendAccountabilityRelationResponse::from)
                        .toList())
                .build();
    }
}
