package com.example.morva.domain.account.user;

import com.example.morva.domain.account.user.vo.UserId;
import com.example.morva.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends AbstractAggregateRoot {

    @EmbeddedId
    private UserId userId;

    @Column(length = 20, nullable = false)
    private String profileName;

    @Column(nullable = false)
    private String profileImage;

    @Column(nullable = false)
    private String passwordHash;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private AuthCredential authCredential;
}
