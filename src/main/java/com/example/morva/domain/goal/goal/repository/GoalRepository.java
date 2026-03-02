package com.example.morva.domain.goal.goal.repository;

import com.example.morva.domain.goal.goal.Goal;
import com.example.morva.domain.goal.goal.vo.GoalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, GoalId> {
}
