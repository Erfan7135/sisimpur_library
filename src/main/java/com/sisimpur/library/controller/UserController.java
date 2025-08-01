package com.sisimpur.library.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import com.sisimpur.library.dto.user.UserResponseDto;
import com.sisimpur.library.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
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
}
