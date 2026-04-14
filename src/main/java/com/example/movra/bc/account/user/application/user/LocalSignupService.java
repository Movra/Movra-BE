package com.example.movra.bc.account.user.application.user;

import com.example.movra.bc.account.user.application.user.dto.request.LocalSignupRequest;
import com.example.movra.bc.account.user.application.user.exception.DuplicateAccountIdException;
import com.example.movra.bc.account.user.application.user.exception.DuplicateEmailException;
import com.example.movra.bc.account.user.application.user.exception.UserCreationFailedException;
import com.example.movra.sharedkernel.file.storage.ImageHelper;
import com.example.movra.bc.account.user.application.user.helper.UserPersister;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.sharedkernel.file.storage.type.ImageType;
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
    private final ImageHelper imageHelper;

    public void signup(LocalSignupRequest localSignupRequest){
        if(userRepository.existsByAccountId(localSignupRequest.accountId())){
            throw new DuplicateAccountIdException();
        }

        if(userRepository.existsByAuthCredentialEmail(localSignupRequest.email())){
            throw new DuplicateEmailException();
        }

        String profileUrl = imageHelper.upload(localSignupRequest.profileImage(), ImageType.PROFILE);

        try{
            userPersister.saveLocalUser(
                    localSignupRequest.accountId(),
                    localSignupRequest.profileName(),
                    profileUrl,
                    localSignupRequest.email(),
                    localSignupRequest.password()
            );
        } catch (DataIntegrityViolationException e){
            imageHelper.cleanup(profileUrl);
            log.warn("회원가입 중복 발생 (레이스 컨디션): {}", e.getMessage());
            throw new DuplicateAccountIdException();
        } catch (Exception e){
            imageHelper.cleanup(profileUrl);
            log.error("회원가입 실패: {}", e.getMessage());
            throw new UserCreationFailedException();
        }
    }
}
