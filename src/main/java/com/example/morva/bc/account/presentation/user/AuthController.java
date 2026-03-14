package com.example.morva.bc.account.presentation.user;

import com.example.morva.bc.account.application.user.LocalLoginService;
import com.example.morva.bc.account.application.user.LocalSignupService;
import com.example.morva.bc.account.application.user.OauthProfileSetupService;
import com.example.morva.bc.account.application.user.ReissueService;
import com.example.morva.bc.account.application.user.dto.request.TokenReissueRequest;
import com.example.morva.bc.account.application.user.dto.request.LocalLoginRequest;
import com.example.morva.bc.account.application.user.dto.request.LocalSignupRequest;
import com.example.morva.bc.account.application.user.dto.request.OauthProfileSetupRequest;
import com.example.morva.bc.account.application.user.dto.response.ProfileSetupResponse;
import com.example.morva.bc.account.application.user.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LocalSignupService localSignupService;
    private final LocalLoginService localLoginService;
    private final OauthProfileSetupService oauthProfileSetupService;
    private final ReissueService reissueService;

    @PostMapping("/signup")
    public void signup(@Valid @ModelAttribute LocalSignupRequest localSignupRequest) {
        localSignupService.signup(localSignupRequest);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LocalLoginRequest localLoginRequest) {
        return localLoginService.login(localLoginRequest);
    }

    @PostMapping("/oauth/profile-setup")
    public ProfileSetupResponse oauthProfileSetup(
            @RequestParam String pendingToken,
            @Valid @ModelAttribute OauthProfileSetupRequest oauthProfileSetupRequest
    ) {
        return oauthProfileSetupService.setup(pendingToken, oauthProfileSetupRequest);
    }

    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody TokenReissueRequest tokenReissueRequest) {
        return reissueService.reissue(tokenReissueRequest);
    }
}
