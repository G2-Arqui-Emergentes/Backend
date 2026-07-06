package pe.edu.upc.taskmaster.backend.iam.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.tokens.jwt.BearerTokenService;
import pe.edu.upc.taskmaster.backend.meeting.application.internal.services.GoogleAccountConnectionService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/google")
public class GoogleOAuth2Controller {

    private final GoogleAccountConnectionService googleAccountConnectionService;
    private final BearerTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public GoogleOAuth2Controller(GoogleAccountConnectionService googleAccountConnectionService,
                                  BearerTokenService tokenService,
                                  @org.springframework.beans.factory.annotation.Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService) {
        this.googleAccountConnectionService = googleAccountConnectionService;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/connect")
    public void connect(HttpServletRequest request,
                        HttpServletResponse response,
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestParam(value = "token", required = false) String token) throws IOException {
        Long userId = resolveUserId(userDetails, token);
        if (userId == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "A valid JWT token is required");
            return;
        }

        var session = request.getSession(true);
        session.setAttribute("google_connection_user_id", userId);
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl userDetails) {
        var status = googleAccountConnectionService.getStatus(userDetails.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("connected", status.connected());
        response.put("googleEmail", status.googleEmail());
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(UserDetailsImpl userDetails, String token) {
        if (userDetails != null) {
            return userDetails.getId();
        }

        if (token == null || token.isBlank() || !tokenService.validateToken(token)) {
            return null;
        }

        try {
            String username = tokenService.getUsernameFromToken(token);
            UserDetails resolvedUser = userDetailsService.loadUserByUsername(username);
            if (resolvedUser instanceof UserDetailsImpl resolved) {
                return resolved.getId();
            }
        } catch (RuntimeException e) {
            return null;
        }

        return null;
    }
}
