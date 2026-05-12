package com.example.movra.bc.analytics.activation_event.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.domain.AnalyticsEvent;
import com.example.movra.bc.analytics.activation_event.domain.repository.AnalyticsEventRepository;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.analytics.activation_funnel.application.service.ActivationFunnelProjector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Clock;
import java.util.Map;

@Slf4j
@Component
public class AnalyticsEventRecorder {

    private final AnalyticsEventRepository analyticsEventRepository;
    private final ActivationFunnelProjector activationFunnelProjector;
    private final Clock clock;
    private final TransactionOperations requiresNewTransaction;

    @Autowired
    public AnalyticsEventRecorder(
            AnalyticsEventRepository analyticsEventRepository,
            ActivationFunnelProjector activationFunnelProjector,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        this(
                analyticsEventRepository,
                activationFunnelProjector,
                clock,
                requiresNewTransaction(transactionManager)
        );
    }

    public AnalyticsEventRecorder(AnalyticsEventRepository analyticsEventRepository, Clock clock) {
        this(
                analyticsEventRepository,
                null,
                clock,
                new TransactionOperations() {
                    @Override
                    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
                        return action.doInTransaction(new SimpleTransactionStatus());
                    }
                }
        );
    }

    private AnalyticsEventRecorder(
            AnalyticsEventRepository analyticsEventRepository,
            ActivationFunnelProjector activationFunnelProjector,
            Clock clock,
            TransactionOperations requiresNewTransaction
    ) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.activationFunnelProjector = activationFunnelProjector;
        this.clock = clock;
        this.requiresNewTransaction = requiresNewTransaction;
    }

    @Transactional
    public AnalyticsEvent record(UserId userId, AnalyticsEventType eventType, Map<String, String> properties) {
        return save(userId, eventType, properties);
    }

    public void recordSafely(UserId userId, AnalyticsEventType eventType, Map<String, String> properties) {
        try {
            requiresNewTransaction.executeWithoutResult(status -> save(userId, eventType, properties));
        } catch (RuntimeException e) {
            log.warn("Analytics event recording failed. eventType={}", eventType, e);
        }
    }

    private static TransactionOperations requiresNewTransaction(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }

    private AnalyticsEvent save(UserId userId, AnalyticsEventType eventType, Map<String, String> properties) {
        AnalyticsEvent analyticsEvent = analyticsEventRepository.save(
                AnalyticsEvent.record(
                        userId,
                        eventType,
                        clock.instant(),
                        properties
                )
        );
        projectActivationFunnel(analyticsEvent);
        return analyticsEvent;
    }

    private void projectActivationFunnel(AnalyticsEvent analyticsEvent) {
        if (activationFunnelProjector == null) {
            return;
        }

        try {
            activationFunnelProjector.project(analyticsEvent);
        } catch (RuntimeException e) {
            log.warn("Activation funnel projection failed. eventType={}", analyticsEvent.getEventType(), e);
        }
    }
}
