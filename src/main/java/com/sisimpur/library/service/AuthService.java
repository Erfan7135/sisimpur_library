package com.sisimpur.library.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sisimpur.library.dto.auth.LoginRequestDto;
import com.sisimpur.library.dto.auth.LoginResponseDto;
import com.sisimpur.library.model.User;
import com.sisimpur.library.repository.UserRepository;
import com.sisimpur.library.util.JwtUtil;
import com.sisimpur.library.exception.ResourceNotFoundException;
import com.sisimpur.library.exception.InvalidCredentialsException;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public LoginResponseDto login(LoginRequestDto loginRequest) {
        logger.info("Attempting to log in user with email: {}", loginRequest.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));
        
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password provided");
        }
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        logger.info("User logged in successfully with email: {}", loginRequest.getEmail());
        return new LoginResponseDto(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
