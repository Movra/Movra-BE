package com.example.morva.domain.collaboration.team_chat.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamChatMessageId(
        UUID teamChatMessageId
) {

    public static TeamChatMessageId newId(){
        return new TeamChatMessageId(UUID.randomUUID());
    }

    public TeamChatMessageId of(UUID teamChatMessageId){
        return new TeamChatMessageId(teamChatMessageId);
    }
}
