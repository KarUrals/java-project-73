package hexlet.code.controller;

import hexlet.code.dto.UserDto;
import hexlet.code.entity.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
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

import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + USER_CONTROLLER_PATH)
@RequiredArgsConstructor
public class UserController {
    public static final String USER_CONTROLLER_PATH = "/users";
    public static final String ID = "/{id}";
    private static final String ONLY_OWNER_BY_ID = """
            @userRepository.findById(#id).get().getEmail() == authentication.getName()
        """;

    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(summary = "Create new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data")
    })
    @PostMapping("/")
    @ResponseStatus(CREATED)
    public User createNewUser(
            @Parameter(description = "User to save", schema = @Schema(implementation = UserDto.class))
            @RequestBody @Valid final UserDto userDto) {
        return userService.createNewUser(userDto);
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User with that ID not found")
    })
    @GetMapping(path = ID)
    public User getUserById(
            @Parameter(description = "ID of user to find")
            @PathVariable final Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Get list of all users")
    @ApiResponse(responseCode = "200", description = "List of all users",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = User.class)))
    @GetMapping("/")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Update existing user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User with that ID not found"),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(path = ID)
    @PreAuthorize(ONLY_OWNER_BY_ID)
    public User updateUser(
            @Parameter(description = "ID of user to update")
            @PathVariable final long id,
            @Parameter(description = "User to update", schema = @Schema(implementation = UserDto.class))
            @RequestBody @Valid final UserDto userDto) {
        return userService.updateUserById(id, userDto);
    }

    @Operation(summary = "Delete user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User with that ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping(path = ID)
    @PreAuthorize(ONLY_OWNER_BY_ID)
    public void deleteUser(
            @Parameter(description = "ID of user to delete")
            @PathVariable final long id) {
        userService.deleteUserById(id);
    }
}
