package com.sisimpur.library.service;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sisimpur.library.exception.ResourceNotFoundException;

import com.sisimpur.library.model.User;
import com.sisimpur.library.model.UserRole;
import com.sisimpur.library.repository.UserRepository;
import com.sisimpur.library.dto.user.UserResponseDto;
import com.sisimpur.library.dto.user.UserCreateRequestDto;
import com.sisimpur.library.dto.user.UserEditRequestDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserResponseDto getUserById(Long id) {
        logger.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if(user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("User with id: " + id + " is not accessible.");
        }
        return new UserResponseDto(user);
    }

    public UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        logger.info("Creating user with email: {}", userCreateRequestDto.getEmail());
        User user = new User();
        user.setName(userCreateRequestDto.getName());
        user.setEmail(userCreateRequestDto.getEmail());
        user.setPasswordHash(userCreateRequestDto.getPassword_hash());
        // user.setRole(UserRole.USER); // Default role for new users
        User savedUser = userRepository.save(user);
        logger.info("User created with id: {}", savedUser.getId());
        return new UserResponseDto(savedUser);
    }

    public UserResponseDto updateUser(Long id, UserEditRequestDto userEditRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        logger.info("Updating user with id: {}", id);
        
        if(userEditRequestDto.getName() != null)
                user.setName(userEditRequestDto.getName());
        if(userEditRequestDto.getEmail() != null)
                user.setEmail(userEditRequestDto.getEmail());
        if(userEditRequestDto.getPasswordHash() != null)
                user.setPasswordHash(userEditRequestDto.getPasswordHash());
        User updatedUser = userRepository.save(user);
        logger.info("User updated with id: {}", updatedUser.getId());
        return new UserResponseDto(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        logger.info("Deleting user with id: {}", id);
        userRepository.delete(user);
        logger.info("User deleted with id: {}", id);
    }

}
