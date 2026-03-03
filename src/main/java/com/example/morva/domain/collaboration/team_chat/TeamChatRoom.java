package com.example.morva.domain.collaboration.team_chat;


import com.example.morva.domain.collaboration.team.vo.TeamId;
import com.example.morva.domain.collaboration.team_chat.vo.TeamChatRoomId;
import com.example.morva.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;



@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamChatRoom extends AbstractAggregateRoot {

    @EmbeddedId
    private TeamChatRoomId teamChatRoomId;

    @Embedded
    private TeamId teamId;

    @Column(length = 15, nullable = false)
    private String name;
}
