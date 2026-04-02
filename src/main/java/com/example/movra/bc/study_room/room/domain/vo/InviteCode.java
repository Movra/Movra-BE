package com.example.movra.bc.study_room.room.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record InviteCode(
        String code
) {

    public static InviteCode generate(){
        return new InviteCode(UUID.randomUUID().toString().substring(0, 8));
    }

    public static InviteCode of(String code){
        return new InviteCode(code);
    }
}
