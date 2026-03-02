package com.example.morva.domain.collaboration.assignment;

import com.example.morva.domain.collaboration.assignment.vo.TeamSubGoalAssignmentId;
import com.example.morva.domain.collaboration.team_goal.vo.TeamSubGoalId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_team_sub_goal_assignments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamSubGoalAssignment {

    @EmbeddedId
    private TeamSubGoalAssignmentId teamSubGoalAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_goal_assignment_id")
    private TeamGoalAssignment teamGoalAssignment;

    @Embedded
    private TeamSubGoalId teamSubGoalId;

    @OneToOne(mappedBy = "teamSubGoalAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private TeamSubGoalAssignmentCompletion teamSubGoalAssignmentCompletion;
}
