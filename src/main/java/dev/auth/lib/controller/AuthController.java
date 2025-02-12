package dev.auth.lib.controller;

import dev.auth.lib.controller.mappers.UsersMapper;
import dev.auth.lib.controller.model.SignUpRequest;
import dev.auth.lib.controller.model.request.LoginRequest;
import dev.auth.lib.controller.model.request.RefreshTokenRequest;
import dev.auth.lib.controller.model.response.LoginResponse;
import dev.auth.lib.controller.model.response.RefreshTokenResponse;
import dev.auth.lib.controller.security.CurrentPrincipal;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.AuthService;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Method to log in a user
     * @param request user access data
     */
    @PostMapping(value = "/sessions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(HttpServletRequest request, @RequestBody @Validated final LoginRequest loginRequest){

        AuthServiceImpl.Tokens tokens = authService.login(loginRequest.getEmail(), loginRequest.getPassword(), request.getRequestURI());
        LoginResponse response = UsersMapper.toLoginResponse(tokens);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Method to log out a user
     */
    @GetMapping(value = "/sessions")
    public ResponseEntity<Void> logout(){

        User user = new CurrentPrincipal().getUser();
        authService.logout(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Method to refresh the token
     * @param request user access data
     */
    @PostMapping(value = "/refresh-token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RefreshTokenResponse> refreshToken (@RequestBody @Validated final RefreshTokenRequest request){

        AuthServiceImpl.Tokens tokens = authService.refreshToken(request.getRefreshToken());
        RefreshTokenResponse response = UsersMapper.toRefreshTokenResponse(tokens);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
