package ru.julia.currencyexchange.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.julia.currencyexchange.application.service.AppUserDetailsService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final AppUserDetailsService userDetailsService;
    private final ApiAuthEntryPoint apiAuthEntryPoint;
    private final ChatIdAccessFilter chatIdAccessFilter;

    @Autowired
    public SecurityConfig(AppUserDetailsService userDetailsService,
                          ApiAuthEntryPoint apiAuthEntryPoint,
                          ChatIdAccessFilter chatIdAccessFilter) {
        this.userDetailsService = userDetailsService;
        this.apiAuthEntryPoint = apiAuthEntryPoint;
        this.chatIdAccessFilter = chatIdAccessFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(chatIdAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(this::configureApiAccess)
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                apiAuthEntryPoint,
                                new AntPathRequestMatcher("/api/**")
                        ))
                .userDetailsService(userDetailsService)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    private void configureApiAccess(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).authenticated()
                .requestMatchers("/api/**").authenticated();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
