package com.example.movra.bc.account.user.presentation.user;

import com.example.movra.bc.account.user.application.user.QueryProfileService;
import com.example.movra.bc.account.user.application.user.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final QueryProfileService queryProfileService;

    @GetMapping("/me")
    public ProfileResponse me() {
        return queryProfileService.query();
    }
}
