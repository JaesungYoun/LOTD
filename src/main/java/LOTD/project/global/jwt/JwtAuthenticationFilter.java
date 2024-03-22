package LOTD.project.global.jwt;

import LOTD.project.domain.member.dto.response.LoginResponse;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;



    private final List<String> EXCLUDE_URL_PATTERN = List.of(
            "/signup", "/login", "/memberId/check", "/nicknames/check", "/oauth/**",
            "/swagger-ui/**","/v3/api-docs/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String pathName = request.getServletPath();
        return EXCLUDE_URL_PATTERN.stream().anyMatch(url -> {
            if (url.endsWith("/**")) {
                return pathName.startsWith(url.substring(0, url.length() - 3));
            } else {
                return url.equalsIgnoreCase(pathName);
            }
        });
    }


    /**
     * 필터 거치지 않는 URL 설정 (로그인이 필요 없는 URL)

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/signup", "/login", "/memberId/check", "/nickname/check", "/oauth/**"};

        //"/oauth/kakao/login", "/oauth/kakao/nickname"
        //        ,"/oauth/naver/login","/oauth/naver/nickname","/oauth/google/login","/oauth/google/nickname"
        // 제외할 url 설정
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String accessToken = jwtService.getAccessToken(request);


        /** AT가 null 이 아닌 경우 */
        if(accessToken != null && jwtService.validateToken(accessToken, request)) { // 1. 토큰이 헤더에 실려왔는지, 토큰이 유효한 토큰인지 확인
            if (!jwtService.checkAccessTokenInBlackList(request,accessToken)) { // 2. 사용자가 로그아웃해서 블랙리스트에 있는 토큰인지 확인
                // accessToken이 유효하면 Context에 Authentication 저장
                    SecurityContextHolder.getContext().setAuthentication(jwtService.getAuthentication(accessToken));
                }
        } else {
            /** AT가 헤더에 없거나, 만료되었거나, 유효하지 않은 경우
             * RT로 AT 재발급 및 API 요청 처리
             */

            String refreshToken = jwtService.getRefreshToken(request);

            if (refreshToken != null && jwtService.validateToken(refreshToken, request) && refreshToken.equals(jwtService.getRedisRefreshToken(refreshToken))) {
                String memberId = jwtService.getMemberId(refreshToken);
                LoginResponse loginResponse = jwtService.createJwtToken(memberId); // AT,RT 재생성

                jwtService.setHeaderAccessToken(response, loginResponse.getAccessToken()); // AT 발급
                jwtService.setHeaderRefreshToken(response, loginResponse.getRefreshToken()); // RT 발급
                jwtService.setRedisRefreshToken(memberId, loginResponse.getRefreshToken()); // RT를 새로 발급했으므로 Redis의 RT 갱신

                this.setAuthentication(loginResponse.getAccessToken());
            } else {
                request.setAttribute("exception", new BaseException(ExceptionCode.ALL_TOKEN_EXPIRED));// 인증 토큰이 없음

            }
        }

        filterChain.doFilter(request, response);
    }
    public void setAuthentication(String accessToken) {
        Authentication authentication = jwtService.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
