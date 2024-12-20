package dev.auth.lib.controller.mappers;

import dev.auth.lib.controller.model.SignUpRequest;
import dev.auth.lib.data.model.User;

public class UsersMapper {

    private UsersMapper(){}

    public static User requestToEntity(SignUpRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }
}
