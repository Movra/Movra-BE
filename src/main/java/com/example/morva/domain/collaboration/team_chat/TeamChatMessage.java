package com.example.morva.domain.collaboration.team_chat;


import com.example.morva.domain.account.user.vo.UserId;
import com.example.morva.domain.collaboration.team_chat.vo.TeamChatMessageId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamChatMessage {

    @EmbeddedId
    private TeamChatMessageId teamChatMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_chat_room_id", nullable = false)
    private TeamChatRoom teamChatRoom;

    @Embedded
    private UserId userId;

    @Column(length = 500, nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
