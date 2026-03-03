package com.example.morva.domain.timer.timer.vo;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TimerId(
        UUID timerId
) implements Serializable {

    public TimerId newId(){
        return new TimerId(UUID.randomUUID());
    }

    public TimerId of(UUID timerId){
        return new TimerId(timerId);
    }
}
