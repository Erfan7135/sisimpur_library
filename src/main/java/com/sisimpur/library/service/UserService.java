package com.sisimpur.library.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.sisimpur.library.exception.ResourceNotFoundException;

import com.sisimpur.library.model.User;
import com.sisimpur.library.model.UserRole;
import com.sisimpur.library.repository.UserRepository;
import com.sisimpur.library.dto.user.UserResponseDto;
import com.sisimpur.library.dto.user.UserCreateRequestDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if(user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("User with id: " + id + " is not accessible.");
        }
        return new UserResponseDto(user);
    }

    public UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        User user = new User();
        user.setName(userCreateRequestDto.getName());
        user.setEmail(userCreateRequestDto.getEmail());
        user.setPasswordHash(userCreateRequestDto.getPassword_hash());
        // user.setRole(UserRole.USER); // Default role for new users
        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

}
