package com.example.morva.sharedkernel.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAggregateRoot {

    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event){
        this.domainEvents.add(event);
    }

    public List<Object> pullDomainEvents(){
        List<Object> events = List.copyOf(this.domainEvents);
        domainEvents.clear();
        return events;
    }
}
