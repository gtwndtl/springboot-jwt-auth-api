package com.gtwndtl.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gtwndtl.demo.dtos.UserDto;
import com.gtwndtl.demo.models.UserModel;
import com.gtwndtl.demo.repositories.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail()))
                .toList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable int id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDto(
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail()
        );
    }

    @PostMapping("/search")
    public UserDto getUserByEmail(@Valid@RequestBody UserDto userDto) {
        UserDto foundUser = userRepository.findByEmail(userDto.getEmail())
            .map(user -> new UserDto(user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail()))
            .orElse(null);

        if (foundUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return foundUser;
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        UserModel user = new UserModel(
                userDto.getUsername(),
                userDto.getEmail(),
                passwordEncoder.encode(userDto.getPassword())
        );
        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());

        UserModel saved = userRepository.save(user);

        return new UserDto(
                saved.getUsername(),
                saved.getFirstname(),
                saved.getLastname(),
                saved.getEmail()
        );
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable int id, @RequestBody UserDto userDto) {

        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }

        if (userDto.getFirstname() != null) {
            user.setFirstname(userDto.getFirstname());
        }

        if (userDto.getLastname() != null) {
            user.setLastname(userDto.getLastname());
        }

        UserModel updated = userRepository.save(user);

        return new UserDto(
                updated.getUsername(),
                updated.getFirstname(),
                updated.getLastname(),
                updated.getEmail()
        );
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}
