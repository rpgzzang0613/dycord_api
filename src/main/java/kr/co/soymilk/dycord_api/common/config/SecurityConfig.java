package kr.co.soymilk.dycord_api.common.config;

import kr.co.soymilk.dycord_api.common.properties.EndpointProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 스프링 시큐리티 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final EndpointProperties endpointProperties;

    /**
     * 액세스토큰을 사용하므로 폼로그인 비활성화, session stateless, stateless이므로 csrf disable
     * 휴대폰에서 테스트할때 대비 사설대역 cors 허용
     * 특정 엔드포인트를 제외하고는 인증없이 접근 못하도록 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(endpointProperties.getPermitAll().toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterAfter(new JwtAuthFilter(), ExceptionTranslationFilter.class);

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
