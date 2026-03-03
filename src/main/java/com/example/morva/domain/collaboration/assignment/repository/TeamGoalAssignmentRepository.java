package com.example.morva.domain.collaboration.assignment.repository;

import com.example.morva.domain.collaboration.assignment.TeamGoalAssignment;
import com.example.morva.domain.collaboration.assignment.vo.TeamGoalAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamGoalAssignmentRepository extends JpaRepository<TeamGoalAssignment, TeamGoalAssignmentId> {
}
