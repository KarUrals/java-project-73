package hexlet.code.service;

import hexlet.code.dto.LabelDto;
import hexlet.code.entity.Label;

public interface LabelService {
    Label createNewLabel(LabelDto labelDto);
    Label updateLabel(long id, LabelDto labelDto);
}
