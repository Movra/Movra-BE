package com.example.movra.bc.account.user.domain.user;

import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import com.example.movra.bc.account.user.domain.user.vo.AuthCredentialId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "tbl_auth_credentials",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_auth_credential_email_provider",
                        columnNames = {"email", "oauth_provider"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthCredential {

    @EmbeddedId
    private AuthCredentialId id;

    @Column(length = 255, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OauthProvider oauthProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    static AuthCredential create(String email, OauthProvider oauthProvider, User user) {
        return new AuthCredential(AuthCredentialId.newId(), email, oauthProvider, user);
    }
}
