package hexlet.code.service.impl;

import hexlet.code.dto.LabelDto;
import hexlet.code.entity.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;

    @Override
    public Label createNewLabel(LabelDto labelDto) {
        final Label label = new Label();
        label.setName(labelDto.getName());
        return labelRepository.save(label);
    }

    @Override
    public Label getLabelById(long id) {
        return labelRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }

    @Override
    public Label updateLabelById(long id, LabelDto labelDto) {
        final Label labelToUpdate = getLabelById(id);
        labelToUpdate.setName(labelDto.getName());
        return labelRepository.save(labelToUpdate);
    }

    @Override
    public void deleteLabelById(long id) {
        getLabelById(id);
        labelRepository.deleteById(id);
    }
}
