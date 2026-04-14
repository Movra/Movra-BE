package com.example.movra.bc.visioning.future_vision.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.vo.FutureVisionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FutureVisionRepository extends JpaRepository<FutureVision, FutureVisionId> {

    Optional<FutureVision> findByUserId(UserId userId);

    boolean existsByUserId(UserId userId);
}
