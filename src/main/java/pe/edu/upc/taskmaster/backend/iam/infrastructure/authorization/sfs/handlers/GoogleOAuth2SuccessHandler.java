package pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pe.edu.upc.taskmaster.backend.meeting.application.internal.services.GoogleAccountConnectionService;

import java.io.IOException;
import java.time.Instant;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GoogleAccountConnectionService googleAccountConnectionService;

    public GoogleOAuth2SuccessHandler(OAuth2AuthorizedClientService authorizedClientService,
                                      GoogleAccountConnectionService googleAccountConnectionService) {
        this.authorizedClientService = authorizedClientService;
        this.googleAccountConnectionService = googleAccountConnectionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauth2Authentication) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauth2Authentication.getAuthorizedClientRegistrationId(),
                    oauth2Authentication.getName()
            );

            if (authorizedClient != null) {
                OAuth2User principal = oauth2Authentication.getPrincipal();
                HttpSession session = request.getSession(false);
                Long userId = null;
                if (session != null) {
                    Object linkedUserId = session.getAttribute("google_connection_user_id");
                    if (linkedUserId instanceof Long value) {
                        userId = value;
                    } else if (linkedUserId instanceof Integer value) {
                        userId = value.longValue();
                    }
                    session.removeAttribute("google_connection_user_id");
                }

                if (userId == null) {
                    throw new IllegalStateException("TaskMaster user is not linked for Google connection");
                }

                String googleEmail = principal.getAttribute("email");
                var accessToken = authorizedClient.getAccessToken();
                var refreshToken = authorizedClient.getRefreshToken();

                googleAccountConnectionService.upsertConnection(
                        userId,
                        googleEmail != null ? googleEmail : oauth2Authentication.getName(),
                        accessToken.getTokenValue(),
                        refreshToken != null ? refreshToken.getTokenValue() : null,
                        accessToken.getExpiresAt() != null ? accessToken.getExpiresAt() : Instant.now().plusSeconds(3600)
                );
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.getWriter().write("Google account connected");
    }
}
