package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long taskStatusId;

    private Long executorId;

    private Set<Long> labelIds;
}
