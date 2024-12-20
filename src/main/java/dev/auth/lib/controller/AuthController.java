package dev.auth.lib.controller;

import dev.auth.lib.controller.mappers.UsersMapper;
import dev.auth.lib.controller.model.SignUpRequest;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Method to register a new user
     * @param request user access data
     */
    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> signUp(@RequestBody @Validated SignUpRequest request){

        User user = UsersMapper.requestToEntity(request);
        authService.signUp(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
