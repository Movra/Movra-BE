package com.example.movra.bc.feedback.tiny_win.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TinyWinId(
        UUID id
) implements Serializable {

    public static TinyWinId newId(){
        return new TinyWinId(UUID.randomUUID());
    }

    public static TinyWinId of(UUID tinyWinId){
        return new TinyWinId(tinyWinId);
    }
}
