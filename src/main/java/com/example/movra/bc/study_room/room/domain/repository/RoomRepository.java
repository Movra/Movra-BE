package com.example.movra.bc.study_room.room.domain.repository;

import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, RoomId> {
}
