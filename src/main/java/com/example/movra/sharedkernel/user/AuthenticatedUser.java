package com.example.movra.sharedkernel.user;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import lombok.Builder;

@Builder
public record AuthenticatedUser(
        UserId userId,
        String accountId,
        String name
) {
}