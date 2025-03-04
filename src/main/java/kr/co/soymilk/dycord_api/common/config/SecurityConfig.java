package kr.co.soymilk.dycord_api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/test").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedOrigins(List.of(
                "http://localhost:3636",
                "https://dycord.co.kr"
        ));
        config.setAllowedOriginPatterns(List.of(
                "http://10.*.*.*:3636",
                "http://172.16.*.*:3636",
                "http://172.17.*.*:3636",
                "http://172.18.*.*:3636",
                "http://172.19.*.*:3636",
                "http://172.20.*.*:3636",
                "http://172.21.*.*:3636",
                "http://172.22.*.*:3636",
                "http://172.23.*.*:3636",
                "http://172.24.*.*:3636",
                "http://172.25.*.*:3636",
                "http://172.26.*.*:3636",
                "http://172.27.*.*:3636",
                "http://172.28.*.*:3636",
                "http://172.29.*.*:3636",
                "http://172.30.*.*:3636",
                "http://172.31.*.*:3636",
                "http://192.168.*.*:3636"
        ));
        config.setAllowedMethods(List.of("OPTIONS", "GET", "POST", "PUT", "DELETE"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
