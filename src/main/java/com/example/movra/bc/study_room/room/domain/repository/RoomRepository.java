package com.example.movra.bc.study_room.room.domain.repository;

import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.vo.InviteCode;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, RoomId> {

    @Query("select r from PublicRoom r")
    List<Room> findAllPublicRooms();

    Optional<Room> findByInviteCode(InviteCode inviteCode);
}
