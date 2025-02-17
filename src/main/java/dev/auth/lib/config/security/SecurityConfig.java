package dev.auth.lib.config.security;

import dev.auth.lib.authentication.CustomAuthenticationEntryPoint;
import dev.auth.lib.authentication.CustomAuthenticationSuccessHandler;
import dev.auth.lib.authentication.provider.BasicAuthProvider;
import dev.auth.lib.config.security.filter.JwtAuthenticationFilter;
import dev.auth.lib.service.authentication.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.cors.allowed-origin}")
    private String allowedOrigin;

    private final BasicAuthProvider basicAuthProvider;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthService authService;

    public SecurityConfig(BasicAuthProvider basicAuthProvider, JwtAuthenticationFilter jwtAuthenticationFilter, AuthService authService) {
        this.basicAuthProvider = basicAuthProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sessionMagConfig -> sessionMagConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(basicAuthProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authReqConfig -> {
                    authReqConfig.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.POST   , "/auth/users").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.POST   , "/auth/sessions").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.POST   , "/auth/sessions/external").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.POST   , "/auth/refresh-token").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.POST   , "/auth/activate").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.POST   , "/auth/recovery-password").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.POST, "/auth/recovery-password/activate").permitAll();
                    authReqConfig.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll();
                    authReqConfig.anyRequest().authenticated();
                })
                .oauth2Login(oauth2Login -> oauth2Login.successHandler(authenticationSuccessHandler()))
                .exceptionHandling(handler -> handler.authenticationEntryPoint(authenticationEntryPoint()))
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(){
        return new CustomAuthenticationSuccessHandler(authService);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return new CustomAuthenticationEntryPoint();
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
