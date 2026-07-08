package com.example.movra.infrastructure.account.user.security.oauth;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.OauthAttribute;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.OauthCallbackPayload;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.OauthCallbackType;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.handler.OauthSuccessHandler;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.OauthCallbackStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OauthSuccessHandlerTest {

    @Mock
    private OauthCallbackStore oauthCallbackStore;

    @Test
    @DisplayName("기존 OAuth 사용자는 callback code만 query로 받아 redirect된다")
    void onAuthenticationSuccess_existingUser_redirectsWithCode() throws Exception {
        // given
        OauthSuccessHandler handler = createHandler();
        User user = User.createOauthUser(
                "testuser",
                "테스트유저",
                "image.png",
                "test@example.com",
                OauthProvider.GOOGLE,
                "encodedPassword"
        );
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(new AuthDetails(user), null);

        given(oauthCallbackStore.save(org.mockito.ArgumentMatchers.any(OauthCallbackPayload.class)))
                .willReturn("callback-code");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        // then
        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://front.example/oauth/callback?code=callback-code");
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();

        ArgumentCaptor<OauthCallbackPayload> payloadCaptor = ArgumentCaptor.forClass(OauthCallbackPayload.class);
        verify(oauthCallbackStore).save(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().type()).isEqualTo(OauthCallbackType.EXISTING_USER);
        assertThat(payloadCaptor.getValue().userId()).isEqualTo(user.getId().id());
    }

    @Test
    @DisplayName("신규 OAuth 사용자는 callback code만 query로 받아 redirect된다")
    void onAuthenticationSuccess_newUser_redirectsWithCode() throws Exception {
        // given
        OauthSuccessHandler handler = createHandler();
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(OauthAttribute.ROLE_PENDING.getKey())),
                Map.of(
                        OauthAttribute.EMAIL.getKey(), "new@example.com",
                        OauthAttribute.PROVIDER.getKey(), OauthProvider.NAVER.name()
                ),
                OauthAttribute.EMAIL.getKey()
        );
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(oauth2User, null);

        given(oauthCallbackStore.save(org.mockito.ArgumentMatchers.any(OauthCallbackPayload.class)))
                .willReturn("callback-code");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        // then
        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://front.example/oauth/callback?code=callback-code");
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();

        ArgumentCaptor<OauthCallbackPayload> payloadCaptor = ArgumentCaptor.forClass(OauthCallbackPayload.class);
        verify(oauthCallbackStore).save(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().type()).isEqualTo(OauthCallbackType.NEW_USER);
        assertThat(payloadCaptor.getValue().email()).isEqualTo("new@example.com");
        assertThat(payloadCaptor.getValue().oauthProvider()).isEqualTo(OauthProvider.NAVER);
    }

    private OauthSuccessHandler createHandler() {
        OauthSuccessHandler handler = new OauthSuccessHandler(oauthCallbackStore);
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://front.example/oauth/callback");
        return handler;
    }
}
