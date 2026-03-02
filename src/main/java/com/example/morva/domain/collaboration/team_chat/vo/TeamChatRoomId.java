package com.example.morva.domain.collaboration.team_chat.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamChatRoomId(
        UUID teamChatRoomId
) {

    public static TeamChatRoomId newId(){
        return new TeamChatRoomId(UUID.randomUUID());
    }

    public TeamChatRoomId of(UUID teamChatRoomId){
        return new TeamChatRoomId(teamChatRoomId);
    }
}
