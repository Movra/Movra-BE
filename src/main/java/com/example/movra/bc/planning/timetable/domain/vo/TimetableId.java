package com.example.movra.bc.planning.timetable.domain.vo;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TimetableId(
        UUID id
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static TimetableId newId(){
        return new TimetableId(UUID.randomUUID());
    }

    public static TimetableId of(UUID timetableId){
        return new TimetableId(timetableId);
    }
}
