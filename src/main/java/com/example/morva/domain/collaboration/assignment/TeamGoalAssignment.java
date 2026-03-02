package com.example.morva.domain.collaboration.assignment;

import com.example.morva.domain.account.user.vo.UserId;
import com.example.morva.domain.collaboration.assignment.vo.TeamGoalAssignmentId;
import com.example.morva.domain.collaboration.team_goal.vo.TeamGoalId;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_team_goal_assignments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamGoalAssignment extends AbstractAggregateRoot<TeamGoalAssignment> {

    @EmbeddedId
    private TeamGoalAssignmentId teamGoalAssignmentId;

    @Embedded
    private TeamGoalId teamGoalId;

    @Embedded
    private UserId userId;

    @Builder.Default
    @OneToMany(mappedBy = "teamGoalAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamSubGoalAssignment> teamSubGoalAssignments = new ArrayList<>();
}
