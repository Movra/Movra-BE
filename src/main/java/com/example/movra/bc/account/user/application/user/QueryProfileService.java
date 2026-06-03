package com.example.movra.bc.account.user.application.user;

import com.example.movra.bc.account.user.application.user.dto.response.ProfileResponse;
import com.example.movra.bc.account.user.application.user.exception.UserNotFoundException;
import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryProfileService {

    private final CurrentUserQuery currentUserQuery;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return ProfileResponse.from(user);
    }
}
