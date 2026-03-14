package com.example.morva.bc.account.application.user;

import com.example.morva.bc.account.application.user.dto.request.LocalSignupRequest;
import com.example.morva.bc.account.application.user.exception.DuplicateAccountIdException;
import com.example.morva.bc.account.application.user.exception.DuplicateEmailException;
import com.example.morva.bc.account.application.user.exception.UserCreationFailedException;
import com.example.morva.bc.account.application.user.helper.UserPersister;
import com.example.morva.bc.account.domain.user.repository.UserRepository;
import com.example.morva.sharedkernel.file.storage.ImageFileStorageService;
import com.example.morva.sharedkernel.file.storage.type.ImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalSignupService {

    private final UserRepository userRepository;
    private final UserPersister userPersister;
    private final ImageFileStorageService imageFileStorageService;

    @Transactional
    public void signup(LocalSignupRequest localSignupRequest){
        if(userRepository.existsByAccountId(localSignupRequest.accountId())){
            throw new DuplicateAccountIdException();
        }

        if(userRepository.existsByAuthCredentialEmail(localSignupRequest.email())){
            throw new DuplicateEmailException();
        }

        String profileUrl = imageFileStorageService.upload(localSignupRequest.profileImage(), ImageType.PROFILE);

        try{
            userPersister.saveLocalUser(
                    localSignupRequest.accountId(),
                    localSignupRequest.profileName(),
                    profileUrl,
                    localSignupRequest.email(),
                    localSignupRequest.password()
            );
        } catch (Exception e){
            cleanupUploadedImage(profileUrl);
            throw new UserCreationFailedException();
        }
    }

    private void cleanupUploadedImage(String profileUrl){
        try{
            imageFileStorageService.deleteByKey(profileUrl);
        } catch (Exception e){
            //Exception 발생해도 그냥 진행
        }
    }
}
