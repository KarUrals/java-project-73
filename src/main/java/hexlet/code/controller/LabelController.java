package hexlet.code.controller;

import hexlet.code.dto.LabelDto;
import hexlet.code.entity.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + LABEL_CONTROLLER_PATH)
@RequiredArgsConstructor
public class LabelController {
    public static final String LABEL_CONTROLLER_PATH = "/labels";
    public static final String ID = "/{id}";

    private final LabelRepository labelRepository;
    private final LabelService labelService;

    @PostMapping(path = "")
    @ResponseStatus(CREATED)
    public Label createNewLabel(@RequestBody @Valid final LabelDto labelDto) {
        return labelService.createNewLabel(labelDto);
    }

    @GetMapping(path = ID)
    public Label getLabelById(@PathVariable final Long id) {
        return labelRepository.findById(id).get();
    }

    @GetMapping(path = "")
    public List<Label> getAllLabels() {
        return labelRepository.findAll()
                .stream()
                .toList();
    }

    @PutMapping(path = ID)
    public Label updateLabel(@PathVariable final long id, @RequestBody @Valid final LabelDto labelDto) {
        return labelService.updateLabel(id, labelDto);
    }

    @DeleteMapping(path = ID)
    public void deleteLabel(@PathVariable final long id) {
        labelRepository.deleteById(id);
    }
}
