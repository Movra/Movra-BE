package com.example.morva.domain.collaboration.team_chat.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TeamChatMessageId(
        UUID teamChatMessageId
) implements Serializable {

    public static TeamChatMessageId newId(){
        return new TeamChatMessageId(UUID.randomUUID());
    }

    public TeamChatMessageId of(UUID teamChatMessageId){
        return new TeamChatMessageId(teamChatMessageId);
    }
}
