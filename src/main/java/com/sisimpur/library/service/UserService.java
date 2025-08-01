package com.sisimpur.library.service;

import org.slf4j.Logger;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.sisimpur.library.exception.ResourceNotFoundException;
import com.sisimpur.library.exception.EmailAlreadyExistsException;
import com.sisimpur.library.exception.EntityWithActiveLendingException;

import com.sisimpur.library.model.User;
import com.sisimpur.library.model.UserRole;
import com.sisimpur.library.model.Lending;
import com.sisimpur.library.repository.LendingRepository;
import com.sisimpur.library.repository.UserRepository;
import com.sisimpur.library.dto.user.UserResponseDto;
import com.sisimpur.library.dto.user.UserCreateRequestDto;
import com.sisimpur.library.dto.user.UserEditRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LendingRepository lendingRepository;
    private final PasswordEncoder passwordEncoder;
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

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        logger.info("Fetching all users with pagination (excluding admins)");
        Page<User> usersPage = userRepository.findByRole(UserRole.USER, pageable);
        return usersPage.map(UserResponseDto::new);
    }

    public UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        logger.info("Creating user with email: {}", userCreateRequestDto.getEmail());
        User user = new User();
        user.setName(userCreateRequestDto.getName());
        user.setEmail(userCreateRequestDto.getEmail());
        if (userRepository.existsByEmail(userCreateRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("A user with email '" + userCreateRequestDto.getEmail() + "' already exists");
        }
        String hashedPassword = passwordEncoder.encode(userCreateRequestDto.getPassword());
        user.setPasswordHash(hashedPassword);
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
        if(userEditRequestDto.getPassword() != null)
                user.setPasswordHash(passwordEncoder.encode(userEditRequestDto.getPassword()));
        User updatedUser = userRepository.save(user);
        logger.info("User updated with id: {}", updatedUser.getId());
        return new UserResponseDto(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        // Check for any active lendings before deletion
        Optional<Lending> lending = lendingRepository.findByUserIdAndReturnDateIsNull(id);
        if (lending.isPresent()) {
            throw new EntityWithActiveLendingException("User with id: " + id + " has active lendings and cannot be deleted.");
        }
        logger.info("Deleting user with id: {}", id);
        userRepository.delete(user);
        logger.info("User deleted with id: {}", id);
    }

    public User getUserEntityById(Long id) {
        logger.info("Fetching user entity with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Page<UserResponseDto> getUsersWithRole(UserRole role, Pageable pageable) {
        logger.info("Fetching users with role: {}", role);
        Page<User> usersPage = userRepository.findByRole(role, pageable);
        return usersPage.map(UserResponseDto::new);
    }

}
