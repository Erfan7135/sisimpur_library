package com.sisimpur.library.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import com.sisimpur.library.dto.user.UserResponseDto;
import com.sisimpur.library.dto.user.UserCreateRequestDto;
import com.sisimpur.library.dto.user.UserEditRequestDto;
import com.sisimpur.library.model.UserRole;

import com.sisimpur.library.service.UserService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable @Min(2) Long id) {
        UserResponseDto userResponseDto = userService.getUserById(id);
        return new ResponseEntity<>(
                userResponseDto,
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
        @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<UserResponseDto> usersPage = userService.getUsersWithRole(UserRole.USER, pageable);
        return new ResponseEntity<>(
            usersPage,
            HttpStatus.OK
        );
    }

    @PostMapping("/")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto userCreateRequestDto) {
        UserResponseDto userResponseDto = userService.createUser(userCreateRequestDto);
        return new ResponseEntity<>(
                userResponseDto,
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserEditRequestDto userEditRequestDto) {
        UserResponseDto userResponseDto = userService.updateUser(id, userEditRequestDto);
        return new ResponseEntity<>(
                userResponseDto,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
