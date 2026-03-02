package com.example.morva.domain.collaboration.team_chat.repository;

import com.example.morva.domain.collaboration.team_chat.TeamChatMessage;
import com.example.morva.domain.collaboration.team_chat.TeamChatRoom;
import com.example.morva.domain.collaboration.team_chat.vo.TeamChatRoomId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamChatRoomRepository extends JpaRepository<TeamChatRoom, TeamChatRoomId> {

    @Query("SELECT m FROM TeamChatMessage m " +
            "WHERE m.teamChatRoom.teamChatRoomId = :chatRoomId " +
            "ORDER BY m.createdAt DESC")
    Page<TeamChatMessage> findMessagesByChatRoomId(
            @Param("chatRoomId") TeamChatRoomId chatRoomId,
            Pageable pageable
    );
}
