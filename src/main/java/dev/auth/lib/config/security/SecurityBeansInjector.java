package dev.auth.lib.config.security;

import dev.auth.lib.authentication.ProvManager;
import dev.auth.lib.authentication.provider.BasicAuthProvider;
import dev.auth.lib.data.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityBeansInjector {

    private UserRepository userRepository;

    @Bean
    public ProvManager provManager(){
        return new ProvManager(authenticationProvider());
    }

    @Bean
    public BasicAuthProvider authenticationProvider(){
        return new BasicAuthProvider(userRepository,passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
