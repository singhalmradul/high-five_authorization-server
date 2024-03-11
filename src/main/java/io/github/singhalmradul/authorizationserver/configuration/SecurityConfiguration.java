package io.github.singhalmradul.authorizationserver.configuration;

import static jakarta.servlet.DispatcherType.FORWARD;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration.applyDefaultSecurity;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @formatter:off
 */
 @Configuration
 @EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    @Order(1)
    DefaultSecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {

        applyDefaultSecurity(http);

        http
            .getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(withDefaults()); // Enable OpenID Connect 1.0
        http
            // Redirect to the login page when not authenticated from the
            // authorization endpoint
            .exceptionHandling(exceptions ->
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(TEXT_HTML)
                    )
            )
            // Accept access tokens for User Info and/or Client Registration
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));

        return http.cors(withDefaults()).build();
    }

    @Bean
    @Order(2)
    DefaultSecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(authorize ->
                authorize
                    .dispatcherTypeMatchers(FORWARD).permitAll()
                    .requestMatchers("/error", "/css/**", "assets/**").permitAll()
                    .anyRequest().authenticated()
            )
            // Form login handles the redirect to the login page from the
            // authorization server filter chain
            .formLogin(form->form.loginPage("/login").permitAll());

        return http.cors(withDefaults()).build();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("http://localhost:3000");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // @Bean
    // InMemoryUserDetailsManager inMemoryUserDetailsManager() {

    //     UserDetails user = withUsername("user")
    //         .password("{noop}password")
    //         .authorities("ROLE_USER")
    //         .build();
    //     return new InMemoryUserDetailsManager(user);
    // }

    @Bean
    @Primary
    JdbcUserDetailsManager userDetialsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }
}