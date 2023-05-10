package hexlet.code;

import org.springframework.dao.DataIntegrityViolationException;

public class AlreadyExistsException extends DataIntegrityViolationException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
