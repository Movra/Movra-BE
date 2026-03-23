package com.example.movra.bc.feedback.tiny_win.presentation;

import com.example.movra.bc.feedback.tiny_win.application.service.CreateTinyWinService;
import com.example.movra.bc.feedback.tiny_win.application.service.DeleteTinyWinService;
import com.example.movra.bc.feedback.tiny_win.application.service.QueryTinyWinService;
import com.example.movra.bc.feedback.tiny_win.application.service.UpdateContentTinyWinService;
import com.example.movra.bc.feedback.tiny_win.application.service.UpdateTitleTinyWinService;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.request.TinyWinRequest;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.request.UpdateContentTInyWinRequest;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.request.UpdateTitleTinyWinRequest;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.response.TinyWinResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tiny-wins")
@RequiredArgsConstructor
public class TinyWinController {

    private final CreateTinyWinService createTinyWinService;
    private final QueryTinyWinService queryTinyWinService;
    private final UpdateTitleTinyWinService updateTitleTinyWinService;
    private final UpdateContentTinyWinService updateContentTinyWinService;
    private final DeleteTinyWinService deleteTinyWinService;

    @PostMapping
    public void create(@Valid @RequestBody TinyWinRequest request) {
        createTinyWinService.create(request);
    }

    @GetMapping
    public List<TinyWinResponse> queryAll() {
        return queryTinyWinService.queryAll();
    }

    @GetMapping("/{tinyWinId}")
    public TinyWinResponse query(@PathVariable UUID tinyWinId) {
        return queryTinyWinService.query(tinyWinId);
    }

    @PatchMapping("/{tinyWinId}/title")
    public void updateTitle(@PathVariable UUID tinyWinId, @Valid @RequestBody UpdateTitleTinyWinRequest request) {
        updateTitleTinyWinService.update(request, tinyWinId);
    }

    @PatchMapping("/{tinyWinId}/content")
    public void updateContent(@PathVariable UUID tinyWinId, @Valid @RequestBody UpdateContentTInyWinRequest request) {
        updateContentTinyWinService.update(request, tinyWinId);
    }

    @DeleteMapping("/{tinyWinId}")
    public void delete(@PathVariable UUID tinyWinId) {
        deleteTinyWinService.delete(tinyWinId);
    }
}
