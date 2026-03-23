package com.example.movra.bc.feedback.tiny_win.application.service;

import com.example.movra.bc.feedback.tiny_win.application.exception.TinyWinNotFoundException;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.response.TinyWinResponse;
import com.example.movra.bc.feedback.tiny_win.domain.repository.TinyWinRepository;
import com.example.movra.bc.feedback.tiny_win.domain.vo.TinyWinId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryTinyWinService {

    private final TinyWinRepository tinyWinRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public List<TinyWinResponse> queryAll() {
        return tinyWinRepository.findAllByUserId(currentUserQuery.currentUser().userId()).stream()
                .map(TinyWinResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TinyWinResponse query(UUID tinyWinId) {
        return tinyWinRepository.findByIdAndUserId(TinyWinId.of(tinyWinId), currentUserQuery.currentUser().userId())
                .map(TinyWinResponse::from)
                .orElseThrow(TinyWinNotFoundException::new);
    }
}
