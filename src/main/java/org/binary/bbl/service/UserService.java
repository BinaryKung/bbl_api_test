package org.binary.bbl.service;

import lombok.RequiredArgsConstructor;
import org.binary.bbl.dto.UserInput;
import org.binary.bbl.entity.User;
import org.binary.bbl.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
    
    public void createUser(UserInput input) {
        if (userRepository.existsByUsernameOrEmail(input.getUsername(), input.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
        }

        User user = new User();
        user.setName(input.getName());
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setWebsite(input.getWebsite());

        userRepository.save(user);
    }

    public void updateUser(Long id, UserInput input) {

        User user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getUsername().equals(input.getUsername()) || !user.getEmail().equals(input.getEmail())) {
            if (userRepository.existsByUsernameOrEmail(input.getUsername(), input.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
            }
        }

        user.setName(input.getName());
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setWebsite(input.getWebsite());
        userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
    
}
