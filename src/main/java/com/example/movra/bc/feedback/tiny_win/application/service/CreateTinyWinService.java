package com.example.movra.bc.feedback.tiny_win.application.service;

import com.example.movra.bc.feedback.tiny_win.application.service.dto.request.TinyWinRequest;
import com.example.movra.bc.feedback.tiny_win.domain.TinyWin;
import com.example.movra.bc.feedback.tiny_win.domain.repository.TinyWinRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateTinyWinService {

    private final TinyWinRepository tinyWinRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void create(TinyWinRequest request){
        tinyWinRepository.save(
                TinyWin.create(
                        currentUserQuery.currentUser().userId(),
                        request.title(),
                        request.content()
                )
        );
    }
}
