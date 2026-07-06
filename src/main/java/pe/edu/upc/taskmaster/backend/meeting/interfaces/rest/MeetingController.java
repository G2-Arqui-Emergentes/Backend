package pe.edu.upc.taskmaster.backend.meeting.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.DeleteMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.queries.GetAllMeetingsQuery;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.queries.GetMeetingByIdQuery;
import pe.edu.upc.taskmaster.backend.meeting.domain.services.MeetingCommandService;
import pe.edu.upc.taskmaster.backend.meeting.domain.services.MeetingQueryService;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources.CreateMeetingResource;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources.MeetingResource;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources.UpdateMeetingResource;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.transform.CreateMeetingCommandFromResourceAssembler;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.transform.MeetingResourceFromEntityAssembler;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.transform.UpdateMeetingCommandFromResourceAssembler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/api/v1/meetings", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Meetings", description = "Meeting Management Endpoints")
public class MeetingController {

    private final MeetingCommandService meetingCommandService;
    private final MeetingQueryService meetingQueryService;

    public MeetingController(MeetingCommandService meetingCommandService, MeetingQueryService meetingQueryService) {
        this.meetingCommandService = meetingCommandService;
        this.meetingQueryService = meetingQueryService;
    }

    private Long getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new RuntimeException("Invalid principal type");
    }

    @PostMapping
    @Operation(summary = "Create a meeting", description = "Creates a new meeting with the authenticated user as the leader")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Meeting created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MeetingResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<?> createMeeting(@RequestBody CreateMeetingResource resource) {
        try {
            Long leaderId = getAuthenticatedUserId();
            var createCommand = CreateMeetingCommandFromResourceAssembler.toCommandFromResource(resource, leaderId);
            var createdMeeting = meetingCommandService.handle(createCommand);

            if (createdMeeting.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Meeting could not be created"));
            }

            var meetingResource = MeetingResourceFromEntityAssembler.toResourceFromEntity(createdMeeting.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(meetingResource);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all meetings", description = "Retrieves a list of all meetings in the system")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Meetings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MeetingResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<MeetingResource>> getAllMeetings() {
        var meetings = meetingQueryService.handle(new GetAllMeetingsQuery());
        var meetingResources = meetings.stream()
                .map(MeetingResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(meetingResources);
    }

    @GetMapping("/{meetingId}")
    @Operation(summary = "Get meeting by ID", description = "Retrieves a specific meeting by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Meeting retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MeetingResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<MeetingResource> getMeetingById(@PathVariable Long meetingId) {
        var meeting = meetingQueryService.handle(new GetMeetingByIdQuery(meetingId));
        if (meeting.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(MeetingResourceFromEntityAssembler.toResourceFromEntity(meeting.get()));
    }

    @PutMapping("/{meetingId}")
    @Operation(summary = "Update a meeting", description = "Updates an existing meeting")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Meeting updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MeetingResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<MeetingResource> updateMeeting(@PathVariable Long meetingId, @RequestBody UpdateMeetingResource resource) {
        try {
            Long leaderId = getAuthenticatedUserId();
            var updateCommand = UpdateMeetingCommandFromResourceAssembler.toCommandFromResource(meetingId, resource);
            var updatedMeeting = meetingCommandService.handle(updateCommand, leaderId);

            if (updatedMeeting.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(MeetingResourceFromEntityAssembler.toResourceFromEntity(updatedMeeting.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{meetingId}")
    @Operation(summary = "Delete a meeting", description = "Deletes a meeting by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meeting deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long meetingId) {
        try {
            Long leaderId = getAuthenticatedUserId();
            meetingCommandService.handle(new DeleteMeetingCommand(meetingId), leaderId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
