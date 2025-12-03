package ch.admin.bit.jeap.jme.cdct.consumer.web.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The consumer's API shall be public.
 */
@Configuration
class ApiSecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 12)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .anyRequest().permitAll());
        return http.build();
    }
}
