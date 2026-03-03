package com.example.morva.domain.timer.timer.repository;

import com.example.morva.domain.timer.timer.Timer;
import com.example.morva.domain.timer.timer.vo.TimerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimerRepository extends JpaRepository<Timer, TimerId> {
}
