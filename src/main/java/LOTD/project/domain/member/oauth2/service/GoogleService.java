package LOTD.project.domain.member.oauth2.service;

import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.Role;
import LOTD.project.domain.member.dto.response.LoginResponse;
import LOTD.project.domain.member.oauth2.SocialType;
import LOTD.project.domain.member.oauth2.dto.request.GoogleSignUpRequest;
import LOTD.project.domain.member.oauth2.dto.response.GoogleInfo;
import LOTD.project.domain.member.oauth2.dto.response.GoogleToken;
import LOTD.project.domain.member.repository.MemberRepository;
import LOTD.project.global.jwt.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleService {

    @Value("${oauth.google.client_id}")
    String clientId;
    @Value("${oauth.google.redirect_uri}")
    String redirectUri;
    @Value("${oauth.google.client_secret}")
    String clientSecret;


    private final MemberRepository memberRepository;
    private final JwtService jwtService;



    /**
     * 카카오 회원가입 및 로그인 (첫 로그인시만)
     * @param
     * @return
     * @throws JsonProcessingException
     */
    public LoginResponse googleSignUpAndLogin(GoogleSignUpRequest googleSignUpRequest, HttpServletResponse response) {

        Member member = Member.builder()
                .memberId(googleSignUpRequest.getGoogleMemberId())
                .password(UUID.randomUUID().toString())
                .nickname(googleSignUpRequest.getNickname())
                .email(googleSignUpRequest.getEmail())
                .role(Role.MEMBER)
                .socialType(SocialType.GOOGLE)
                .build();

        // 회원 저장
        memberRepository.save(member);

        // 토큰 발급

        LoginResponse loginResponse = jwtService.createJwtToken(member.getMemberId());

        jwtService.setHeaderAccessToken(response, loginResponse.getAccessToken());
        jwtService.setHeaderRefreshToken(response, loginResponse.getRefreshToken());

        return LoginResponse.builder()
                .memberId(member.getMemberId())
                .grantType(loginResponse.getGrantType())
                .accessToken(loginResponse.getAccessToken())
                .accessTokenExpiresIn(loginResponse.getAccessTokenExpiresIn())
                .refreshToken(loginResponse.getRefreshToken())
                .refreshTokenExpiresIn(loginResponse.getRefreshTokenExpiresIn())
                .socialType(SocialType.GOOGLE)
                .build();
    }

    /**
     * 카카오 로그인 (우선 로그인 버튼 눌리면 인가 코드 받아서 토큰까지 발급, 그리고 기존 회원인지, 아닌지 구분
     * @param code
     * @param response
     * @return
     * @throws JsonProcessingException
     */


    public LoginResponse googleLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        GoogleToken googleToken = getGoogleToken(code);

        String accessToken = googleToken.getAccessToken();

        Member member = getMember(accessToken);

        if (member == null) {
            return LoginResponse.builder().accessToken(accessToken).build();
        }
        else {
            LoginResponse loginResponse = jwtService.createJwtToken(member.getMemberId());

            jwtService.setHeaderAccessToken(response, loginResponse.getAccessToken());
            jwtService.setHeaderRefreshToken(response, loginResponse.getRefreshToken());

            return LoginResponse.builder()
                    .memberId(member.getMemberId())
                    .grantType(loginResponse.getGrantType())
                    .accessToken(loginResponse.getAccessToken())
                    .accessTokenExpiresIn(loginResponse.getAccessTokenExpiresIn())
                    .refreshToken(loginResponse.getRefreshToken())
                    .refreshTokenExpiresIn(loginResponse.getRefreshTokenExpiresIn())
                    .socialType(SocialType.GOOGLE)
                    .build();
        }
    }


    public GoogleInfo getMemberInfoToSend(String accessToken) throws JsonProcessingException {
        JsonNode jsonNode = getGoogleInfo(accessToken);

        // DB 에 중복된 Google 소셜 회원 Id 가 있는지 확인
        String googleId = (jsonNode.get("id") + "@g").replaceAll("\"","");
        String email = null;
        if (jsonNode.get("email") != null) {
            email = jsonNode.get("email").asText();
        }
        return GoogleInfo.builder()
                .googleMemberId(googleId)
                .email(email)
                .build();
    }

    private Member getMember(String accessToken) throws JsonProcessingException {
        JsonNode jsonNode = getGoogleInfo(accessToken);

        // DB 에 중복된 Google 소셜 Id 가 있는지 확인
        String googleId = (jsonNode.get("id") + "@g").replaceAll("\"","");
        Member googleMember = memberRepository.findByMemberId(googleId).orElse(null);

        if (googleMember == null) {
            return null;
        }
        return googleMember;
    }

    private GoogleToken getGoogleToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("client_secret",clientSecret);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                googleTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);


        String accessToken = jsonNode.get("access_token").asText();
        String refreshToken = jsonNode.get("refresh_token").asText();
        String tokenType = jsonNode.get("token_type").asText();
        int expiresIn = jsonNode.get("expires_in").asInt();
        String scope = jsonNode.get("scope").asText();

        GoogleToken googleToken = GoogleToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .scope(scope)
                .build();

        return googleToken;
    }

    /**
     * accessToken으로 카카오 사용자 정보 얻기
     */
    private JsonNode getGoogleInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken); // 헤더에 accessToken 담아서 사용자 정보 요청
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://www.googleapis.com/userinfo/v2/me",
                HttpMethod.GET,
                googleUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(responseBody);

    }
}
