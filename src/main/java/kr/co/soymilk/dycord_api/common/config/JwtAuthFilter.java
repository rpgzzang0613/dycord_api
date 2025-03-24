package kr.co.soymilk.dycord_api.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO 회원정보 DB 추가되면 request 헤더에 담긴 액세스 토큰 꺼내서 토큰 유효성, 회원여부 검증 및 후처리

        filterChain.doFilter(request, response);
    }
}
