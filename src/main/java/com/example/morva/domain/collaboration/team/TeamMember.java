package com.example.morva.domain.collaboration.team;

import com.example.morva.domain.account.user.vo.UserId;
import com.example.morva.domain.collaboration.team.type.Role;
import com.example.morva.domain.collaboration.team.vo.TeamMemberId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "tbl_team_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_id", "user_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMember {

    @EmbeddedId
    private TeamMemberId teamMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Embedded
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
