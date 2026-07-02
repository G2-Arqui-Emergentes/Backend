package pe.edu.upc.taskmaster.backend.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.taskmaster.backend.iam.domain.model.queries.GetAllRolesQuery;
import pe.edu.upc.taskmaster.backend.iam.domain.services.RoleQueryService;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.RoleResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.RoleResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Roles", description = "Role Management Endpoints")
public class RolesController {

  private final RoleQueryService roleQueryService;

  public RolesController(RoleQueryService roleQueryService) {
    this.roleQueryService = roleQueryService;
  }

  @GetMapping
  @Operation(
          summary = "Get all roles",
          description = "Retrieves a list of all available roles in the system"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Roles retrieved successfully",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleResource.class))
          ),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  })
  public ResponseEntity<List<RoleResource>> getAllRoles() {
    var getAllRolesQuery = new GetAllRolesQuery();
    var roles = roleQueryService.handle(getAllRolesQuery);
    var roleResources = roles.stream()
            .map(RoleResourceFromEntityAssembler::toResourceFromEntity)
            .toList();
    return ResponseEntity.ok(roleResources);
  }
}
