package com.example.movra.bc.home.today.presentation;

import com.example.movra.bc.home.today.application.service.QueryHomeTodayService;
import com.example.movra.bc.home.today.application.service.dto.response.HomeTodayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final QueryHomeTodayService queryHomeTodayService;

    @GetMapping("/today")
    public HomeTodayResponse queryToday() {
        return queryHomeTodayService.query();
    }
}
