package com.example.morva.domain.collaboration.team.repository;

import com.example.morva.domain.collaboration.team.Team;
import com.example.morva.domain.collaboration.team.vo.TeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, TeamId> {
}
