package com.example.movra.bc.personalization.behavior_profile.domain.repository;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.vo.BehaviorProfileId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BehaviorProfileRepository extends JpaRepository<BehaviorProfile, BehaviorProfileId> {

    boolean existsByUserId(UserId userId);

    Optional<BehaviorProfile> findByUserId(UserId userId);
}
