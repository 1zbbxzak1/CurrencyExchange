package ru.julia.currencyexchange.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.julia.currencyexchange.application.service.AppUserDetailsService;
import ru.julia.currencyexchange.domain.enums.RoleEnum;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final AppUserDetailsService userDetailsService;
    private final ApiAuthEntryPoint apiAuthEntryPoint;

    public SecurityConfig(AppUserDetailsService userDetailsService, ApiAuthEntryPoint apiAuthEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.apiAuthEntryPoint = apiAuthEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(this::configureApiAccess)
                .authorizeHttpRequests(this::configureHtmlAccess)
                .formLogin(this::configureFormLogin)
                .logout(this::configureLogout)
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                apiAuthEntryPoint,
                                new AntPathRequestMatcher("/api/**")
                        ))
                .userDetailsService(userDetailsService)
                .addFilterBefore(new RedirectFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configureApiAccess(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/**").authenticated();
    }

    private void configureHtmlAccess(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/", "/auth", "/register").anonymous()
                .requestMatchers("/static/**", "/css/**").permitAll()
                .requestMatchers("/swagger-ui/**").hasRole(RoleEnum.ADMIN.name())
                .anyRequest().authenticated();
    }

    private void configureFormLogin(FormLoginConfigurer<HttpSecurity> form) {
        form
                .loginPage("/auth")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/currency-rates/list", true)
                .permitAll();
    }

    private void configureLogout(LogoutConfigurer<HttpSecurity> logout) {
        logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
