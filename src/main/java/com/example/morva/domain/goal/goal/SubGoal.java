package com.example.morva.domain.goal.goal;

import com.example.morva.domain.goal.goal.type.SubGoalStatus;
import com.example.morva.domain.goal.goal.vo.SubGoalId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_sub_goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubGoal {

    @EmbeddedId
    private SubGoalId subGoalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubGoalStatus subGoalStatus;
}
