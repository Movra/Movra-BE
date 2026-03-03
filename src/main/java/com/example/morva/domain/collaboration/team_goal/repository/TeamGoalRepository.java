package com.example.morva.domain.collaboration.team_goal.repository;

import com.example.morva.domain.collaboration.team_goal.TeamGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamGoalRepository extends JpaRepository<TeamGoal, TeamGoalRepository> {
}
