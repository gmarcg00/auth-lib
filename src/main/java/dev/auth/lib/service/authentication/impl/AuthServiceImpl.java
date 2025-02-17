package dev.auth.lib.service.authentication.impl;

import dev.auth.lib.authentication.model.AuthenticationDTO;
import dev.auth.lib.authentication.model.AuthenticationRequestDetails;
import dev.auth.lib.data.model.*;
import dev.auth.lib.exception.*;
import dev.auth.lib.service.authentication.AuthService;
import dev.auth.lib.service.authentication.ExchangeCodeService;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.service.authentication.RefreshTokenService;
import dev.auth.lib.service.email.EmailService;
import dev.auth.lib.service.users.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static dev.auth.lib.service.email.types.EmailFormatterFactory.createUserRecoveryPasswordEmailFormatter;
import static dev.auth.lib.service.email.types.EmailFormatterFactory.createUserRegistrationEmailFormatter;
import static java.util.Objects.isNull;


@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String INVALID_CREDENTIALS_ERROR = "Credentials provided by the user are not valid.";
    private static final String USER_NOT_FOUND_ERROR = "User not found.";

    @Value("${server.host.front:local}")
    private String hostFrontend;

    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private EmailService emailService;
    private ExchangeCodeService exchangeCodeService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService, EmailService emailService,ExchangeCodeService exchangeCodeService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.exchangeCodeService = exchangeCodeService;
    }

    @Transactional
    @Override
    public void signUp(User user) {
       User savedUser = userService.createUser(user,false);
       emailService.sendEmail(user.getEmail(), createUserRegistrationEmailFormatter(savedUser, hostFrontend));
    }

    @Override
    public Tokens login(String email, String password, String requestUri) {
        AuthenticationDTO authDTO = AuthenticationDTO.builder()
                .user(new User(email, password))
                .details(new AuthenticationRequestDetails(requestUri))
                .authenticated(false)
                .build();
        Authentication authentication = authenticationManager.authenticate(authDTO);
        if(!authentication.isAuthenticated()){
            log.info("Las credenciales suministradas por el usuario no son válidas.");
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_ERROR);
        }
        User user = (User) authentication.getPrincipal();
        return generateTokens(user);
    }

    @Override
    public Optional<ExchangeSessionCode> externalAccess(String email) {
        var opUser = userService.findByEmail(email);
        if(opUser.isPresent()) return Optional.ofNullable(exchangeCodeService.create(opUser.get()));
        User user = User.builder()
                .email(email)
                .build();
        User savedUser = userService.createUser(user,true);
        emailService.sendEmail(user.getEmail(), createUserRegistrationEmailFormatter(savedUser, hostFrontend));
        return Optional.empty();
    }

    @Transactional
    @Override
    public void logout(User user) {
        if(isNull(user)) throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
        refreshTokenService.deleteByUser(user);
    }

    @Transactional
    @Override
    public Tokens refreshToken(String token) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenService.findByToken(token);
        RefreshToken refreshToken = optionalRefreshToken
                .orElseThrow(() -> {
                    log.warn("El token de refresco ya no existe.");
                    return new RefreshTokenNotFoundException("Refresh token not found.");
                });
        if(!refreshTokenService.isRefreshTokenValid(refreshToken)) {
            log.info("El token de refresco ha expirado.");
            throw new RefreshTokenExpiredException("Refresh token has expired.");
        }
        User user = refreshToken.getUser();
        return this.generateTokens(user);
    }

    @Transactional
    @Override
    public void activateUser(String email, String verificationCode, String password) {
        try {
            userService.activateUser(email, verificationCode, password);
        } catch (UserNotFoundException | InvalidVerificationCodeException e) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_ERROR);
        }
    }

    @Transactional
    @Override
    public void changePassword(String email, String userPassword, String oldPassword) {
        try {
            userService.changePassword(email, userPassword, oldPassword);
        } catch (InvalidPasswordException e) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_ERROR);
        }
    }

    @Transactional
    @Override
    public void recoveryPassword(String email) {
        User user = userService.enableResetPassword(email);
        emailService.sendEmail(user.getEmail(), createUserRecoveryPasswordEmailFormatter(user, hostFrontend));
    }

    @Override
    public void recoveryPasswordActivate(String email, String verificationCode, String password) {
        try {
            userService.recoveryPasswordActivate(email, verificationCode, password);
        } catch (UserNotFoundException | InvalidVerificationCodeException e) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_ERROR);
        }
    }

    @Override
    public Tokens externalLogin(String code) {
        ExchangeSessionCode exchangeSessionCode = exchangeCodeService.validate(code);
        exchangeCodeService.delete(exchangeSessionCode);
        return generateTokens(exchangeSessionCode.getUser());
    }

    private AuthServiceImpl.Tokens generateTokens(User user) {
        checkIfUserIsActive(user);
        AccessToken accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthServiceImpl.Tokens(accessToken, refreshToken);
    }


    private static void checkIfUserIsActive(User user) {
        if (!UserStatusEnum.ACTIVE.getStatusCode().equals(user.getStatus().getName())) {
            log.warn("Intento de acceso con usuario no activo: {}", user.getId());
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_ERROR);
        }
    }

    @AllArgsConstructor
    @Data
    public static class Tokens {
        private AccessToken accessToken;
        private RefreshToken refreshToken;

    }
}
