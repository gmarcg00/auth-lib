package dev.auth.lib.authentication.provider;

import dev.auth.lib.authentication.model.AuthenticationDTO;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RequiredArgsConstructor
public class BasicAuthProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User principal = (User) authentication.getPrincipal();
        String email = principal.getUsername();
        String password = authentication.getCredentials().toString();
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if(passwordEncoder.matches(password, user.getPassword())){
                return new AuthenticationDTO(user,null,true);
            }
        }
        return new AuthenticationDTO(null,null,false);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
