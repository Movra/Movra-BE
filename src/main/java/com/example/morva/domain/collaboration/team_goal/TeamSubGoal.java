package com.example.morva.domain.collaboration.team_goal;

import com.example.morva.domain.collaboration.team_goal.type.TeamSubGoalStatus;
import com.example.morva.domain.collaboration.team_goal.vo.TeamGoalId;
import com.example.morva.domain.collaboration.team_goal.vo.TeamSubGoalId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_team_sub_goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamSubGoal {

    @EmbeddedId
    private TeamSubGoalId teamSubGoalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_goal_id")
    private TeamGoal teamGoal;

    @Column(length = 100, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamSubGoalStatus subGoalStatus;
}
