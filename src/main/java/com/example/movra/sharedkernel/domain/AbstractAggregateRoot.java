package com.example.movra.sharedkernel.domain;

import jakarta.persistence.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAggregateRoot {

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    @DomainEvents
    public List<Object> domainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
