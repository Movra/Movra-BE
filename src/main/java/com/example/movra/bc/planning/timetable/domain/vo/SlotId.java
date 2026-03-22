package com.example.movra.bc.planning.timetable.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record SlotId(
        UUID id
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static SlotId newId(){
        return new SlotId(UUID.randomUUID());
    }

    public static SlotId of(UUID slotId){
        return new SlotId(slotId);
    }

}
