package com.example.movra.bc.planning.timetable.domain.vo;


import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TimetableId(
        UUID id
) {
    public static TimetableId newId(){
        return new TimetableId(UUID.randomUUID());
    }

    public static TimetableId of(UUID timetableId){
        return new TimetableId(timetableId);
    }
}
