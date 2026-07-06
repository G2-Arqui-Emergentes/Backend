package pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pe.edu.upc.taskmaster.backend.meeting.application.internal.services.GoogleAccountConnectionService;
import pe.edu.upc.taskmaster.backend.meeting.application.internal.services.GoogleOAuthStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuth2SuccessHandler.class);

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GoogleAccountConnectionService googleAccountConnectionService;
    private final GoogleOAuthStateService googleOAuthStateService;
    private final String frontendOrigin;

    public GoogleOAuth2SuccessHandler(OAuth2AuthorizedClientService authorizedClientService,
                                      GoogleAccountConnectionService googleAccountConnectionService,
                                      GoogleOAuthStateService googleOAuthStateService,
                                      @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.authorizedClientService = authorizedClientService;
        this.googleAccountConnectionService = googleAccountConnectionService;
        this.googleOAuthStateService = googleOAuthStateService;
        this.frontendOrigin = normalizeOrigin(frontendUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        boolean connected = false;
        String message = "Google authorization completed.";

        try {
            if (authentication instanceof OAuth2AuthenticationToken oauth2Authentication) {
                OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                        oauth2Authentication.getAuthorizedClientRegistrationId(),
                        oauth2Authentication.getName()
                );

                if (authorizedClient != null) {
                    OAuth2User principal = oauth2Authentication.getPrincipal();
                    String state = request.getParameter("state");
                    Long userId = googleOAuthStateService.consumeUserId(state);
                    if (userId == null) {
                        LOGGER.warn("Google OAuth callback completed without valid state for principal {}", oauth2Authentication.getName());
                        message = "Google authorization completed, but the OAuth state was not found or expired.";
                    } else {
                        String googleEmail = principal.getAttribute("email");
                        var accessToken = authorizedClient.getAccessToken();
                        var refreshToken = authorizedClient.getRefreshToken();

                        googleAccountConnectionService.upsertConnection(
                                userId,
                                Optional.ofNullable(googleEmail).orElse(oauth2Authentication.getName()),
                                accessToken.getTokenValue(),
                                refreshToken != null ? refreshToken.getTokenValue() : null,
                                accessToken.getExpiresAt() != null ? accessToken.getExpiresAt() : Instant.now().plusSeconds(3600)
                        );
                        connected = true;
                        message = "Google Calendar connected successfully.";
                    }
                } else {
                    LOGGER.warn("Google OAuth callback completed without authorized client for principal {}", oauth2Authentication.getName());
                    message = "Google authorization completed, but the authorized client was not available.";
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("Google OAuth callback failed: {}", e.getMessage(), e);
            message = "Google authorization failed: " + e.getMessage();
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(buildResponsePage(connected, message));
        response.getWriter().flush();
    }

    private String buildResponsePage(boolean connected, String message) {
        String eventType = connected ? "GOOGLE_CONNECTED" : "GOOGLE_CONNECTION_ERROR";
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <title>Google Calendar</title>
                  <meta charset="UTF-8">
                </head>
                <body>
                  <script>
                    if (window.opener) {
                      window.opener.postMessage(
                        { type: %s, message: %s },
                        '%s'
                      );
                    }
                    window.close();
                  </script>
                  <p>%s</p>
                </body>
                </html>
                """.formatted(toJsString(eventType), toJsString(message), frontendOrigin, escapeHtml(message));
    }

    private String normalizeOrigin(String frontendUrl) {
        try {
            URI uri = URI.create(frontendUrl);
            if (uri.getScheme() == null || uri.getAuthority() == null) {
                return "http://localhost:5173";
            }
            return uri.getScheme() + "://" + uri.getAuthority();
        } catch (IllegalArgumentException e) {
            return "http://localhost:5173";
        }
    }

    private String toJsString(String value) {
        String safe = value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
        return "'" + safe + "'";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
