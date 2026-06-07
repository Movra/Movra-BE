package com.example.movra.bc.focus.focus_session.application.service.dto.request;

/**
 * 집중 세션 시작 요청.
 * <p>
 * {@code presetMinutes}가 비어 있으면(null) 무제한(오픈엔드) 타이머로 시작하고,
 * 값이 있으면 허용된 프리셋({@code 3, 5, 10, 25}) 중 하나여야 한다(도메인에서 검증).
 */
public record StartFocusSessionRequest(
        Integer presetMinutes
) {
}
