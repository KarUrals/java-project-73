package hexlet.code.service.impl;

import hexlet.code.dto.UserDto;
import hexlet.code.entity.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createNewUser(final UserDto userDto) {
        final User user = createUserFromDto(userDto);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUserById(long id, UserDto userDto) {
        final User userToUpdate = createUserFromDto(userDto);
        userToUpdate.setId(id);
        return userRepository.save(userToUpdate);
    }

    @Override
    public void deleteUserById(long id) {
        final User userToDelete = getUserById(id);
        userRepository.delete(userToDelete);
    }

    @Override
    public String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public User getCurrentUser() {
        return userRepository.findByEmail(getCurrentUserName())
                .orElseThrow(NoSuchElementException::new);
    }

    private User createUserFromDto(final UserDto userDto) {

        return User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();
    }
}
