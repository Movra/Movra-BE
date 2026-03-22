package com.example.movra.bc.visioning.future_vision.domain.vo;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record FutureVisionId(
        UUID id
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static FutureVisionId newId(){
        return new FutureVisionId(UUID.randomUUID());
    }

    public static FutureVisionId of(UUID futureVisionId){
        return new FutureVisionId(futureVisionId);
    }
}
