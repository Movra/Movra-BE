package com.example.morva.domain.calendar.calendar.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TodoCalendarId(
        UUID todoCalendarId
) {

    public static TodoCalendarId newId(){
        return new TodoCalendarId(UUID.randomUUID());
    }

    public TodoCalendarId of(UUID todoCalendarId){
        return new TodoCalendarId(todoCalendarId);
    }
}
