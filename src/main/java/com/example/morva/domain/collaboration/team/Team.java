package com.example.morva.domain.collaboration.team;

import com.example.morva.domain.collaboration.team.vo.TeamId;
import com.example.morva.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Team extends AbstractAggregateRoot {

    @EmbeddedId
    private TeamId teamId;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private String profileImage;

    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private InviteCode inviteCode;

    @Builder.Default
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers = new ArrayList<>();
}
