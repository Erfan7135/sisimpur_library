package com.sisimpur.library.dto.user;

import lombok.Getter;
import lombok.Setter;

import com.sisimpur.library.model.User;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
