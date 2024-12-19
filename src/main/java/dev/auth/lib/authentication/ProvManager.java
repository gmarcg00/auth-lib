package dev.auth.lib.authentication;

import dev.auth.lib.authentication.model.AuthenticationRequestDetails;
import dev.auth.lib.authentication.provider.BasicAuthProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@AllArgsConstructor
public class ProvManager implements AuthenticationManager {

    public static final String LOGIN_PATH = "/auth/sessions";
    private BasicAuthProvider basicAuthProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthenticationRequestDetails authRequestDetails = (AuthenticationRequestDetails) authentication.getDetails();
        if(authRequestDetails.getUri().equals(LOGIN_PATH)){
            return basicAuthProvider.authenticate(authentication);
        }
        return null;
    }
}
