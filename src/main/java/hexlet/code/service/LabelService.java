package hexlet.code.service;

import hexlet.code.dto.LabelDto;
import hexlet.code.entity.Label;

import java.util.List;

public interface LabelService {
    Label createNewLabel(LabelDto labelDto);
    Label getLabelById(long id);
    List<Label> getAllLabels();
    Label updateLabelById(long id, LabelDto labelDto);
    void deleteLabelById(long id);
}
