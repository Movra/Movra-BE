package com.example.movra.bc.feedback.tiny_win.application.service;

import com.example.movra.bc.feedback.tiny_win.application.exception.TinyWinNotFoundException;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.request.UpdateContentTInyWinRequest;
import com.example.movra.bc.feedback.tiny_win.domain.TinyWin;
import com.example.movra.bc.feedback.tiny_win.domain.repository.TinyWinRepository;
import com.example.movra.bc.feedback.tiny_win.domain.vo.TinyWinId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateContentTinyWinService {

    private final TinyWinRepository tinyWinRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void update(UpdateContentTInyWinRequest request, UUID tinyWinId){
        TinyWin tinyWin = tinyWinRepository.findByIdAndUserId(TinyWinId.of(tinyWinId), currentUserQuery.currentUser().userId())
                .orElseThrow(TinyWinNotFoundException::new);

        tinyWin.updateContent(request.content());

        tinyWinRepository.save(tinyWin);
    }
}
