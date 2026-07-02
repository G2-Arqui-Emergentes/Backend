package pe.edu.upc.taskmaster.backend.ai.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.taskmaster.backend.ai.domain.model.queries.GetLeaderDashboardQuery;
import pe.edu.upc.taskmaster.backend.ai.domain.model.queries.GetMemberDashboardQuery;
import pe.edu.upc.taskmaster.backend.ai.domain.services.AIQueryService;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.DashboardResponseResource;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.MemberDashboardResponseResource;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.WeeklySummaryResource;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.transform.DashboardResponseFromQueryAssembler;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.transform.MemberDashboardResponseFromQueryAssembler;
import pe.edu.upc.taskmaster.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.taskmaster.backend.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.taskmaster.backend.project.infrastructure.persistence.jpa.repositories.ProjectRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "AI Dashboard", description = "AI-Powered Endpoints")
@Slf4j
@RequiredArgsConstructor
public class AIController {

    private final AIQueryService aiQueryService;
    private final DashboardResponseFromQueryAssembler dashboardAssembler;
    private final MemberDashboardResponseFromQueryAssembler memberDashboardAssembler;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    /**
     * Retrieves the authenticated user ID from the security context
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        throw new RuntimeException("Invalid principal type");
    }

    /**
     * Validates that the user has the LEADER role
     */
    private void validateLeaderRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isLeader = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Roles.ROLE_LEADER);

        if (!isLeader) {
            throw new RuntimeException("Access denied: LEADER role required");
        }
    }

    /**
     * Validates that the user has the MEMBER role
     */
    private void validateMemberRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isMember = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Roles.ROLE_MEMBER);

        if (!isMember) {
            throw new RuntimeException("Access denied: MEMBER role required");
        }
    }

    @GetMapping("/leader")
    @Operation(
            summary = "Get leader dashboard",
            description = "Retrieves an AI-powered dashboard for the authenticated leader. The leader ID is obtained from the JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponseResource.class))
            ),
            @ApiResponse(responseCode = "403", description = "User does not have the LEADER role"),
            @ApiResponse(responseCode = "404", description = "No projects found for the leader"),
            @ApiResponse(responseCode = "500", description = "Internal error generating the dashboard")
    })
    public ResponseEntity<DashboardResponseResource> getLeaderDashboard() {
        try {
            Long leaderId = getAuthenticatedUserId();
            validateLeaderRole(leaderId);

            var projects = projectRepository.findByLeaderId(leaderId);

            if (projects.isEmpty()) {
                return ResponseEntity.ok(new DashboardResponseResource(
                        null,
                        List.of("You have no projects assigned as a leader"),
                        Collections.emptyList(),
                        LocalDate.now().toString(),
                        0,
                        0
                ));
            }

            var query = new GetLeaderDashboardQuery(leaderId);
            var insights = aiQueryService.handle(query);

            if (insights.isEmpty()) {
                return ResponseEntity.ok(new DashboardResponseResource(
                        null,
                        List.of("Insufficient data to generate the dashboard"),
                        Collections.emptyList(),
                        LocalDate.now().toString(),
                        projects.size(),
                        0
                ));
            }

            var response = dashboardAssembler.toResource(insights, leaderId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error in leader dashboard: {}", e.getMessage());
            if (e.getMessage().contains("Access denied") || e.getMessage().contains("role required")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/member")
    @Operation(
            summary = "Get member dashboard",
            description = "Retrieves an AI-powered weekly dashboard for the authenticated member. The member ID is obtained from the JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberDashboardResponseResource.class))
            ),
            @ApiResponse(responseCode = "403", description = "User does not have the MEMBER role"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "500", description = "Internal error generating the dashboard")
    })
    public ResponseEntity<MemberDashboardResponseResource> getMemberDashboard() {
        try {
            Long memberId = getAuthenticatedUserId();
            validateMemberRole(memberId);

            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            if (member.getMemberInProjects().isEmpty()) {
                return ResponseEntity.ok(new MemberDashboardResponseResource(
                        new WeeklySummaryResource(0.0, "You are not assigned to any project", "No data", LocalDate.now().toString()),
                        List.of("Request to join a project to receive recommendations")
                ));
            }

            var query = new GetMemberDashboardQuery(memberId);
            var summary = aiQueryService.handle(query);

            var response = memberDashboardAssembler.toResource(summary);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error in member dashboard: {}", e.getMessage());
            if (e.getMessage().contains("Access denied") || e.getMessage().contains("role required")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
