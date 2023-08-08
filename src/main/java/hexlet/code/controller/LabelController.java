package hexlet.code.controller;

import hexlet.code.dto.LabelDto;
import hexlet.code.entity.Label;
import hexlet.code.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;
import java.util.List;

import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + LABEL_CONTROLLER_PATH)
@RequiredArgsConstructor
public class LabelController {
    public static final String LABEL_CONTROLLER_PATH = "/labels";
    public static final String ID = "/{id}";
    public static final String AUTHORIZED_USERS_ONLY = "isAuthenticated()";

    private final LabelService labelService;
    @Operation(summary = "Create new label")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Label created",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PostMapping("/")
    @ResponseStatus(CREATED)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public Label createNewLabel(
            @Parameter(description = "Label to save", schema = @Schema(implementation = LabelDto.class))
            @RequestBody @Valid final LabelDto labelDto) {
        return labelService.createNewLabel(labelDto);
    }
    @Operation(summary = "Get label by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label found",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "404", description = "Label with that ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @GetMapping(path = ID)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public Label getLabelById(
            @Parameter(description = "ID of label to find")
            @PathVariable final Long id) {
        return labelService.getLabelById(id);
    }
    @Operation(summary = "Get list of all labels")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all labels",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @GetMapping("/")
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public List<Label> getAllLabels() {
        return labelService.getAllLabels();
    }
    @Operation(summary = "Update existing label by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label updated",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "404", description = "Label with that ID not found"),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PutMapping(path = ID)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public Label updateLabel(
            @Parameter(description = "ID of label to update")
            @PathVariable final long id,
            @Parameter(description = "Label to update", schema = @Schema(implementation = LabelDto.class))
            @RequestBody @Valid final LabelDto labelDto) {
        return labelService.updateLabelById(id, labelDto);
    }
    @Operation(summary = "Delete label by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label deleted"),
            @ApiResponse(responseCode = "404", description = "Label with that ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @DeleteMapping(path = ID)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public void deleteLabel(
            @Parameter(description = "ID of label to delete")
            @PathVariable final long id) {
        labelService.deleteLabelById(id);
    }
}
