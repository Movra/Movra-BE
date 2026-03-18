package com.example.movra.bc.planning.timetable.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record SlotId(
        UUID id
) {

    public static SlotId newId(){
        return new SlotId(UUID.randomUUID());
    }

    public static SlotId of(UUID slotId){
        return new SlotId(slotId);
    }

}
