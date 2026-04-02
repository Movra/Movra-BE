package com.example.movra.bc.study_room.participant.domain.repository;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.vo.ParticipantId;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
    Optional<Participant> findByUserIdAndRoomId(UserId userId, RoomId roomId);
    boolean existsByUserIdAndRoomId(UserId userId, RoomId roomId);
    boolean existsByRoomId(RoomId roomId);
    List<Participant> findAllByRoomId(RoomId roomId);
    Optional<Participant> findFirstByRoomIdOrderByJoinedAtAsc(RoomId roomId);
    List<Participant> findAllByUserId(UserId userId);
}
