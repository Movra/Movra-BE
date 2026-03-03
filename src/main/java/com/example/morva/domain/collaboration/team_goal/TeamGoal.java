package com.example.morva.domain.collaboration.team_goal;

import com.example.morva.domain.collaboration.team.vo.TeamId;
import com.example.morva.domain.collaboration.team_goal.type.TeamGoalStatus;
import com.example.morva.domain.collaboration.team_goal.vo.TeamGoalId;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_team_goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamGoal {

    @EmbeddedId
    private TeamGoalId teamGoalId;

    @Embedded
    private TeamId teamId;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(nullable = false)
    private float progressPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamGoalStatus teamGoalStatus;

    @Builder.Default
    @OneToMany(mappedBy = "teamGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamSubGoal> teamSubGoals = new ArrayList<>();
}

