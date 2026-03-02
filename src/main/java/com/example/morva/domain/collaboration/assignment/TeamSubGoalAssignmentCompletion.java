package com.example.morva.domain.collaboration.assignment;

import com.example.morva.domain.collaboration.assignment.type.CompletionStatus;
import jakarta.persistence.*;
import lombok.*;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_team_sub_goal_assignment_completions")
@IdClass(TeamSubGoalAssignmentCompletion.CompletionId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamSubGoalAssignmentCompletion {

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionId implements Serializable {
        private UUID teamSubGoalAssignmentId;
        private UUID userId;
    }

    @Id
    @Column(name = "team_sub_goal_assignment_id")
    private UUID teamSubGoalAssignmentId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_sub_goal_assignment_id", insertable = false, updatable = false)
    private TeamSubGoalAssignment teamSubGoalAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompletionStatus completionStatus;
}
