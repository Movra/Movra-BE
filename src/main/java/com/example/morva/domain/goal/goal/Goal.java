package com.example.morva.domain.goal.goal;

import com.example.morva.domain.account.user.vo.UserId;
import com.example.morva.domain.goal.goal.type.GoalStatus;
import com.example.morva.domain.goal.goal.vo.GoalId;
import com.example.morva.domain.goal.goal.vo.SubGoalId;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Goal {

    @EmbeddedId
    private GoalId goalId;

    @Embedded
    private UserId userId;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(nullable = false)
    private float progressPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus goalStatus;

    @Builder.Default
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubGoal> subGoals = new ArrayList<>();
}
