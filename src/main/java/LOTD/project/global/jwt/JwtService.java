package LOTD.project.global.jwt;


import LOTD.project.domain.member.dto.response.LoginResponse;

import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import LOTD.project.global.login.service.CustomUserDetailsService;
import LOTD.project.global.login.service.redis.RedisService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private Key key;
    private final RedisService redisService;
    private final CustomUserDetailsService customUserDetailsService;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Value("${jwt.access.expiration}")
    private Long accessExpirePeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpirePeriod;

    public LoginResponse createJwtToken(String memberId){

        long now = (new Date()).getTime();

        Date accessTokenExpiresIn = new Date(now+ accessExpirePeriod);
        Date refreshTokenExpiresIn = new Date(now + refreshExpirePeriod);

        // Access Token 생성
        String accessToken = createAccessToken(memberId,accessTokenExpiresIn);
        // Refresh Token 생성
        String refreshToken = createRefreshToken(memberId,refreshTokenExpiresIn);



        return LoginResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshTokenExpiresIn(refreshTokenExpiresIn.getTime())
                .build();
    }

    public String createAccessToken(String memberId, Date expirationTime){
        return Jwts.builder()
                .setSubject(TokenType.ACCESS.name())    // 토큰 제목
                .setIssuedAt(new Date())                // 토큰 발급 시간
                .setExpiration(expirationTime)          // 토큰 만료 시간
                .claim("memberId", memberId)                  // 회원 아이디
                .signWith(key)
                .compact();

    }

    public String createRefreshToken(String memberId, Date expirationTime) {
        return Jwts.builder()
                .setSubject(TokenType.REFRESH.name())   // 토큰 제목
                .setIssuedAt(new Date())                // 토큰 발급 시간
                .setExpiration(expirationTime)          // 토큰 만료 시간
                .claim("memberId", memberId)      // 회원 아이디
                .signWith(key)
                .setHeaderParam("typ", "JWT")
                .compact();
    }



    // Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드


    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token,HttpServletRequest request) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | io.jsonwebtoken.security.SignatureException e) {
            log.info("Invalid JWT Token", e);
            request.setAttribute("exception",new BaseException(ExceptionCode.NOT_VALID_TOKEN));
            return false;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            redisService.deleteAccessTokenInBlackList(token);
            request.setAttribute("exception", new BaseException(ExceptionCode.TOKEN_EXPIRED));
            return false;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
            request.setAttribute("exception",new BaseException(ExceptionCode.UNSUPPORTED_TOKEN));
            return false;
        } catch (IllegalArgumentException | DecodingException e) {
            log.info("JWT claims string is empty.", e);
            request.setAttribute("exception",new BaseException(ExceptionCode.NOT_ACCESS_TOKEN_TYPE));
            return false;
        }
    }

    public boolean checkAccessTokenInBlackList(HttpServletRequest request, String accessToken) {
        if (redisService.isTokenInBlacklist(accessToken,getExpiration(accessToken))) {
            request.setAttribute("exception", new BaseException(ExceptionCode.BLACKLISTED_ACCESS_TOKEN));
            throw new BaseException(ExceptionCode.BLACKLISTED_ACCESS_TOKEN);
        }
        return false;
    }

    // accessToken
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        
        if(authorizationHeader != null && !authorizationHeader.equals("")){
            if (authorizationHeader.startsWith("Bearer") && authorizationHeader.length() > 7) {
                String accessToken = authorizationHeader.substring(7); // accesstoken 추출
                return accessToken;
            }
        }

        return null; // 헤더 비어있으면 null 리턴
    }


    public String getRefreshToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization-refresh");


        if(authorizationHeader != null && !authorizationHeader.equals("")){
            if (authorizationHeader.startsWith("Bearer") && authorizationHeader.length() > 7) {
                String refreshToken = authorizationHeader.substring(7); // refresh 추출
                return refreshToken;
            }
            else {
                request.setAttribute("exception",new BaseException(ExceptionCode.ALL_TOKEN_EXPIRED));// 인증 토큰이 없음
                throw new BaseException(ExceptionCode.ALL_TOKEN_EXPIRED);
            }
        }
        return null; // 헤더 비어있으면 null 리턴
    }

    public String getRedisRefreshToken(String refreshToken) {
        if (refreshToken != null) {
            Claims claims = parseClaims(refreshToken);
            String memberId = claims.get("memberId", String.class);
            String redisRefreshToken = redisService.getRefreshToken(memberId);
            return redisRefreshToken;
        }
        return null;
    }

    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(this.getMemberId(accessToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


    public String getMemberId(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        return claims.get("memberId", String.class);
    }

    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    public void setRedisRefreshToken(String memberId, String refreshToken) {
        redisService.setRefreshToken(memberId,refreshToken);
    }


    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader("Authorization", "Bearer " + accessToken);
    }
    public void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
        response.setHeader("Authorization-refresh", "Bearer " + refreshToken);
    }
}


