package com.example.movra.bc.account.user.domain.user;

import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends AbstractAggregateRoot {

    @EmbeddedId
    private UserId id;

    @Column(length = 20, unique = true, nullable = false)
    private String accountId;

    @Column(length = 20, nullable = false)
    private String profileName;

    @Column(nullable = false)
    private String profileImage;

    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthCredential> authCredentials = new ArrayList<>();

    public static User createLocalUser(
            String accountId,
            String profileName,
            String profileImage,
            String email,
            String passwordHash
    ) {
        User user = User.builder()
                .id(UserId.newId())
                .accountId(accountId)
                .profileName(profileName)
                .profileImage(profileImage)
                .passwordHash(passwordHash)
                .build();

        user.addCredential(email, OauthProvider.LOCAL);
        return user;
    }

    public static User createOauthUser(
            String accountId,
            String profileName,
            String profileImage,
            String email,
            OauthProvider oauthProvider,
            String passwordHash
    ) {
        User user = User.builder()
                .id(UserId.newId())
                .accountId(accountId)
                .profileName(profileName)
                .profileImage(profileImage)
                .passwordHash(passwordHash)
                .build();

        user.addCredential(email, oauthProvider);
        return user;
    }

    public void addCredential(String email, OauthProvider oauthProvider) {
        AuthCredential credential = AuthCredential.create(email, oauthProvider, this);
        this.authCredentials.add(credential);
    }

    public boolean hasCredential(String email, OauthProvider oauthProvider) {
        return authCredentials.stream()
                .anyMatch(c -> c.getEmail().equals(email) && c.getOauthProvider() == oauthProvider);
    }
}
