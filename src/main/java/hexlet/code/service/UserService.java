package hexlet.code.service;

import hexlet.code.dto.UserDto;
import hexlet.code.entity.User;

public interface UserService {
    User createNewUser(UserDto userDto);
    User updateCurrentUser(long id, UserDto userDto);
}
