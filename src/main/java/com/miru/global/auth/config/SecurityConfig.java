package com.miru.global.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miru.global.auth.filter.BanRestrictionFilter;
import com.miru.global.auth.filter.PendingUserFilter;
import com.miru.global.auth.handler.OAuth2LoginFailureHandler;
import com.miru.global.auth.handler.OAuth2LoginSuccessHandler;
import com.miru.global.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.miru.global.auth.service.CustomOAuth2UserService;
import com.miru.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정
 *
 * <p>전체 보안 정책을 설정한다:
 * <ul>
 *   <li>CORS: 모든 오리진 허용 (개발 환경), credentials 포함</li>
 *   <li>CSRF: 쿠키 기반 토큰(XSRF-TOKEN), H2 콘솔 경로 예외 처리</li>
 *   <li>OAuth2: Google/Naver/Kakao 소셜 로그인, 쿠키 기반 state 저장</li>
 *   <li>인가: 관리자(/api/admin/**), 인증 필요(/api/mypage/**, /api/inquiries/**), 일부 공개(GET 게시판 등)</li>
 *   <li>커스텀 필터: PendingUserFilter(약관 미동의 차단), BanRestrictionFilter(정지 유저 차단)</li>
 *   <li>역할 계층: ADMIN > USER (ADMIN이 USER 권한 포함)</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final PendingUserFilter pendingUserFilter;
    private final BanRestrictionFilter banRestrictionFilter;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 개발 환경 - 전체 허용
        configuration.addAllowedOriginPattern("*");

        // 2. 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 3. 허용할 헤더 (인증 관련 헤더 포함)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-XSRF-TOKEN"));

        // 4. 매우 중요: 쿠키/세션 정보를 주고받으려면 true로 설정해야 함
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 API 경로에 적용
        return source;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {

        // 계층 관계 설정 (ADMIN > USER 관계)
        return RoleHierarchyImpl.withRolePrefix("ROLE_")
                .role("ADMIN").implies("USER").build();

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler, OAuth2LoginFailureHandler oAuth2LoginFailureHandler) throws Exception {

        // csrf 토큰 처리 (H2 콘솔 경로는 예외)
        // Spring Security 6에서 기본 핸들러가 XorCsrfTokenRequestAttributeHandler로 변경되어
        // 쿠키값과 X-XSRF-TOKEN 헤더값이 불일치하는 문제 → CsrfTokenRequestAttributeHandler로 교체
        http
                .csrf(csrf -> {
                    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
                    csrf.csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/h2-console/**");
                });

        // H2 콘솔 iframe 허용
        http
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // formlogin 방식 사용 X
        http
                .formLogin((login) -> login.disable());


        http
                .httpBasic((basic) -> basic.disable());

        // OAuth2 로그인 처리 필터
        http
                .oauth2Login((oauth2) -> oauth2
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestRepository(cookieAuthorizationRequestRepository))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                        .userInfoEndpoint(userInfoEndpointConfig ->
                                userInfoEndpointConfig.userService(customOAuth2UserService)));

        // 비로그인 접근 시 401 + ApiResponse 형식으로 반환
        http
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                            new ObjectMapper().writeValue(response.getWriter(),
                                    ApiResponse.error("로그인 세션이 만료되었습니다. 다시 로그인해 주세요."));
                        }));

        // 페이지 별 인가 설정
        http
                .authorizeHttpRequests((auth) -> auth
                        // 관리자 전용 페이지 권한 설정
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 자기분석 페이지 권한 설정 (비 로그인 유저도 입장 가능)
                        .requestMatchers(HttpMethod.GET, "/api/analysis").permitAll()

                        // 게시판 페이지 권한 설정 (비 로그인 유저도 게시판 조회 및 검색 가능)
                        .requestMatchers(HttpMethod.GET, "/api/boards", "/api/boards/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/boards/{id}").permitAll()

                        // 약관 동의 API (PENDING 유저도 접근 가능)
                        .requestMatchers("/api/agreements").authenticated()

                        // 로그인 유저 정보 조회
                        .requestMatchers("/api/me").authenticated()

                        // 그 외 기본적인 인증 필요 페이지 (마이 페이지, 문의 페이지)
                        .requestMatchers("/api/mypage/**").authenticated()
                        .requestMatchers("/api/inquiries/**").authenticated()

                        // 모든 사람 이용 가능
                        .requestMatchers("/error","/favicon.ico").permitAll()
                        .requestMatchers("/", "/oauth/**", "/login/**").permitAll()
                        // H2 콘솔 (개발 환경)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated());

        // 로그아웃
        http
                .logout(logout -> logout
                        // 프론트에서 이 주소로 POST 요청 보내면 로그아웃
                        .logoutUrl("/api/logout")

                        // 로그아웃 성공 시 ApiResponse 형식으로 반환
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                            new ObjectMapper().writeValue(response.getWriter(),
                                    ApiResponse.success("로그아웃 되었습니다.", null));
                        })

                        // 세션 및 쿠키 삭제
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN"));

        // PENDING 유저 접근 제한 필터 등록
        http.addFilterAfter(pendingUserFilter, UsernamePasswordAuthenticationFilter.class);
        // BAN 유저 게시글/댓글 작성 제한 필터 등록
        http.addFilterAfter(banRestrictionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
