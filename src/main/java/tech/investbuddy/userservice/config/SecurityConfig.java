package tech.investbuddy.userservice.config;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable() // Disable CSRF for simplicity
            .authorizeRequests()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow preflight requests
            .anyRequest().authenticated(); // Secure other endpoints
        return http.build();
    }
}
