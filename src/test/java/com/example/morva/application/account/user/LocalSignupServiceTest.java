package com.example.morva.application.account.user;

import com.example.morva.bc.account.application.user.LocalSignupService;
import com.example.morva.bc.account.application.user.dto.request.LocalSignupRequest;
import com.example.morva.bc.account.application.user.exception.DuplicateAccountIdException;
import com.example.morva.bc.account.application.user.exception.DuplicateEmailException;
import com.example.morva.bc.account.application.user.helper.UserPersister;
import com.example.morva.bc.account.domain.user.repository.UserRepository;
import com.example.morva.sharedkernel.file.storage.ImageFileStorageService;
import com.example.morva.sharedkernel.file.storage.type.ImageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LocalSignupServiceTest {

    @InjectMocks
    private LocalSignupService localSignupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPersister userPersister;

    @Mock
    private ImageFileStorageService imageFileStorageService;

    private LocalSignupRequest createSignupRequest() {
        return new LocalSignupRequest(
                "test@example.com",
                "testuser",
                "테스트유저",
                new MockMultipartFile("profileImage", "image.png", "image/png", "image".getBytes()),
                "password123"
        );
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        LocalSignupRequest request = createSignupRequest();
        given(userRepository.existsByAccountId(request.accountId())).willReturn(false);
        given(userRepository.existsByAuthCredentialEmail(request.email())).willReturn(false);
        given(imageFileStorageService.upload(request.profileImage(), ImageType.PROFILE)).willReturn("uploaded-url");

        // when
        localSignupService.signup(request);

        // then
        then(userPersister).should().saveLocalUser(
                eq(request.accountId()),
                eq(request.profileName()),
                eq("uploaded-url"),
                eq(request.email()),
                eq(request.password())
        );
    }

    @Test
    @DisplayName("중복된 accountId로 회원가입 시 DuplicateAccountIdException 발생")
    void signup_duplicateAccountId_throwsException() {
        // given
        LocalSignupRequest request = createSignupRequest();
        given(userRepository.existsByAccountId(request.accountId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> localSignupService.signup(request))
                .isInstanceOf(DuplicateAccountIdException.class);
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 DuplicateEmailException 발생")
    void signup_duplicateEmail_throwsException() {
        // given
        LocalSignupRequest request = createSignupRequest();
        given(userRepository.existsByAccountId(request.accountId())).willReturn(false);
        given(userRepository.existsByAuthCredentialEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> localSignupService.signup(request))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
