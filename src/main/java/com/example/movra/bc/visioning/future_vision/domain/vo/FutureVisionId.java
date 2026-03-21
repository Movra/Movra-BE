package com.example.movra.bc.visioning.future_vision.domain.vo;


import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record FutureVisionId(
        UUID id
) {

    public static FutureVisionId newId(){
        return new FutureVisionId(UUID.randomUUID());
    }

    public static FutureVisionId of(UUID futureVisionId){
        return new FutureVisionId(futureVisionId);
    }
}
