package com.example.movra.bc.account.application.user;

import com.example.movra.bc.account.application.user.dto.request.LocalSignupRequest;
import com.example.movra.bc.account.application.user.exception.DuplicateAccountIdException;
import com.example.movra.bc.account.application.user.exception.DuplicateEmailException;
import com.example.movra.bc.account.application.user.exception.UserCreationFailedException;
import com.example.movra.bc.account.application.user.helper.ProfileImageHelper;
import com.example.movra.bc.account.application.user.helper.UserPersister;
import com.example.movra.bc.account.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalSignupService {

    private final UserRepository userRepository;
    private final UserPersister userPersister;
    private final ProfileImageHelper profileImageHelper;

    public void signup(LocalSignupRequest localSignupRequest){
        if(userRepository.existsByAccountId(localSignupRequest.accountId())){
            throw new DuplicateAccountIdException();
        }

        if(userRepository.existsByAuthCredentialEmail(localSignupRequest.email())){
            throw new DuplicateEmailException();
        }

        String profileUrl = profileImageHelper.upload(localSignupRequest.profileImage());

        try{
            userPersister.saveLocalUser(
                    localSignupRequest.accountId(),
                    localSignupRequest.profileName(),
                    profileUrl,
                    localSignupRequest.email(),
                    localSignupRequest.password()
            );
        } catch (DataIntegrityViolationException e){
            profileImageHelper.cleanup(profileUrl);
            log.warn("회원가입 중복 발생 (레이스 컨디션): {}", e.getMessage());
            throw new DuplicateAccountIdException();
        } catch (Exception e){
            profileImageHelper.cleanup(profileUrl);
            log.error("회원가입 실패: {}", e.getMessage());
            throw new UserCreationFailedException();
        }
    }
}
