package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest,
                                    @NonNull HttpServletResponse httpResponse,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String authorizationHeader = httpRequest.getHeader("Authorization");

        // 토큰이 없는 요청도 있을 수 있으므로, 토큰이 없으면 일단 다음 필터로 넘김
        if (authorizationHeader == null) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            log.warn("잘못된 형식의 Authorization 헤더: {}", authorizationHeader);
            sendErrorResponse(httpResponse, HttpStatus.BAD_REQUEST, "잘못된 형식의 Authorization 헤더입니다.");
            return;
        }

        String jwt = jwtUtil.substringToken(authorizationHeader);

        if (!processAuthentication(jwt, httpRequest, httpResponse)) {
            return;
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    private boolean processAuthentication(String jwt, HttpServletRequest httpRequest,
                                          HttpServletResponse httpResponse) throws IOException {
        try {
            Claims claims = jwtUtil.extractClaims(jwt);

            if (claims == null) {
                sendErrorResponse(httpResponse, HttpStatus.BAD_REQUEST, "잘못된 JWT 토큰입니다.");
                return false; // 검증 실패
            }

            // SecurityContext에 인증 정보가 없으면 설정 (이미 인증된 경우 중복 설정 방지)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(claims);
            }

            return true; // 검증 성공
        } catch (ExpiredJwtException e) {
            log.info("JWT 만료: userId={}, URI={}", e.getClaims().getSubject(), httpRequest.getRequestURI());
            sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다.");
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            log.error("JWT 검증 실패 [{}]: URI={}", e.getClass().getSimpleName(), httpRequest.getRequestURI(), e);
            sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
        } catch (Exception e) {
            log.error("예상치 못한 오류: URI={}", httpRequest.getRequestURI(), e);
            sendErrorResponse(httpResponse, HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다.");
        }
        return false; // 검증 실패
    }

    // JWT Claims에서 사용자 정보를 추출하여 Spring Security의 인증 정보 설정
    private void setAuthentication(Claims claims) {

        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));
        String nickname = claims.get("nickname", String.class);

        AuthUser authUser = new AuthUser(userId, email, userRole, nickname);
        Authentication authenticationToken = new JwtAuthenticationToken(authUser);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
