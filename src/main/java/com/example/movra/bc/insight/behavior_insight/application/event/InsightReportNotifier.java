package com.example.movra.bc.insight.behavior_insight.application.event;

import com.example.movra.bc.insight.behavior_insight.domain.event.InsightReportGeneratedEvent;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

/**
 * 분석 리포트 생성(영속화)이 커밋된 뒤 완료 알림을 보낸다.
 * AFTER_COMMIT을 쓰는 이유: 리포트가 실제로 저장된 경우에만 푸시가 나가도록 보장하기 위함.
 * data 페이로드에 리포트 식별자·딥링크를 담아 클라이언트가 알림에서 바로 조회할 수 있게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsightReportNotifier {

    private static final String TITLE = "이번 달 분석 리포트가 도착했어요";
    private static final String BODY = "지난 30일의 집중 패턴과 맞춤 인사이트를 확인해보세요.";
    private static final String DATA_KEY_REPORT_ID = "insightReportId";
    private static final String DATA_KEY_LINK = "link";

    private final NotificationGateway notificationGateway;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInsightReportGenerated(InsightReportGeneratedEvent event) {
        UUID reportId = event.insightReportId().id();
        NotificationPayload payload = NotificationPayload.of(
                NotificationType.INSIGHT_REPORT_READY,
                TITLE,
                BODY,
                Map.of(
                        DATA_KEY_REPORT_ID, reportId.toString(),
                        DATA_KEY_LINK, "/insights/" + reportId
                )
        );

        notificationGateway.sendSafely(event.userId(), payload);
        log.info("Insight 완료 알림 발송 요청 - userId={}, reportId={}", event.userId().id(), reportId);
    }
}
