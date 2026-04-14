package com.example.movra.bc.focus.focus_session.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.vo.FocusSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, FocusSessionId> {

    boolean existsByUserIdAndEndedAtIsNull(UserId userId);

    Optional<FocusSession> findByUserIdAndEndedAtIsNull(UserId userId);

    @Query("""
            SELECT fs
            FROM FocusSession fs
            WHERE fs.userId = :userId
              AND fs.startedAt < :periodEnd
              AND (fs.endedAt IS NULL OR fs.endedAt > :periodStart)
            ORDER BY fs.startedAt ASC
            """)
    List<FocusSession> findAllOverlappingPeriod(
            @Param("userId") UserId userId,
            @Param("periodStart") Instant periodStart,
            @Param("periodEnd") Instant periodEnd
    );
}
