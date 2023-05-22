package hexlet.code.service.implementation;

import hexlet.code.dto.LabelDto;
import hexlet.code.entity.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
    public Label updateLabel(long id, LabelDto labelDto) {
        final Label labelToUpdate = labelRepository.getById(id);
        labelToUpdate.setName(labelDto.getName());
        return labelRepository.save(labelToUpdate);
    }
}
