package com.example.movra.bc.feedback.tiny_win.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.tiny_win.domain.TinyWin;
import com.example.movra.bc.feedback.tiny_win.domain.vo.TinyWinId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TinyWinRepository extends JpaRepository<TinyWin, TinyWinId> {
    Optional<TinyWin> findByIdAndUserId(TinyWinId id, UserId userId);
    List<TinyWin> findAllByUserId(UserId userId);
}
