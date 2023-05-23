package hexlet.code.service;

import hexlet.code.dto.UserDto;
import hexlet.code.entity.User;

import java.util.List;

public interface UserService {
    User createNewUser(UserDto userDto);
    User getUserById(long id);
    List<User> getAllUsers();
    User updateUserById(long id, UserDto userDto);
    void deleteUserById(long id);
    String getCurrentUserName();
    User getCurrentUser();
}
