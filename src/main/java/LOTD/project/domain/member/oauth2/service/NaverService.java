package LOTD.project.domain.member.oauth2.service;


import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.Role;
import LOTD.project.domain.member.dto.response.LoginResponse;
import LOTD.project.domain.member.oauth2.SocialType;
import LOTD.project.domain.member.oauth2.dto.request.NaverSignUpRequest;
import LOTD.project.domain.member.oauth2.dto.response.NaverInfo;
import LOTD.project.domain.member.oauth2.dto.response.NaverToken;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NaverService {

    @Value("${oauth.naver.client_id}")
    String clientId;
    @Value("${oauth.naver.redirect_uri}")
    String redirectUri;
    @Value("${oauth.naver.client_secret}")
    String clientSecret;


    private final MemberRepository memberRepository;
    private final JwtService jwtService;



    /**
     * 카카오 회원가입 및 로그인 (첫 로그인시만)
     * @param
     * @return
     * @throws JsonProcessingException
     */
    public LoginResponse naverSignUpAndLogin(NaverSignUpRequest naverSignUpRequest, HttpServletResponse response) {

        Member member = Member.builder()
                .memberId(naverSignUpRequest.getNaverMemberId())
                .password(UUID.randomUUID().toString())
                .nickname(naverSignUpRequest.getNickname())
                .email(naverSignUpRequest.getEmail())
                .role(Role.MEMBER)
                .socialType(SocialType.NAVER)
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
                .socialType(SocialType.NAVER)
                .build();
    }

    /**
     * 카카오 로그인 (우선 로그인 버튼 눌리면 인가 코드 받아서 토큰까지 발급, 그리고 기존 회원인지, 아닌지 구분
     * @param code
     * @param response
     * @return
     * @throws JsonProcessingException
     */


    public LoginResponse naverLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        NaverToken naverToken = getNaverToken(code);

        String accessToken = naverToken.getAccessToken();

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
                    .socialType(SocialType.NAVER)
                    .build();
        }
    }


    public NaverInfo getMemberInfoToSend(String accessToken) throws JsonProcessingException {
        JsonNode jsonNode = getNaverInfo(accessToken);

        // DB 에 중복된 Naver Id 가 있는지 확인
        String naverId = (jsonNode.get("response").get("id") + "@n").replaceAll("\"","");
        String email = null;
        if (jsonNode.get("response").get("email") != null) {
            email = jsonNode.get("response").get("email").asText();
        }
        return NaverInfo.builder()
                .naverMemberId(naverId)
                .email(email)
                .build();
    }

    private Member getMember(String accessToken) throws JsonProcessingException {
        JsonNode jsonNode = getNaverInfo(accessToken);

        // DB 에 중복된 Naver Id 가 있는지 확인
        String naverId = (jsonNode.get("response").get("id") + "@n").replaceAll("\"","");
        Member naverMember = memberRepository.findByMemberId(naverId).orElse(null);

        if (naverMember == null) {
            return null;
        }
        return naverMember;
    }

    private NaverToken getNaverToken(String code) throws JsonProcessingException {
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
        HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                naverTokenRequest,
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
        //String error = jsonNode.get("error").asText();
        //String error_description = jsonNode.get("error_description").asText();

        NaverToken naverToken = NaverToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .build();

        return naverToken;
    }

    /**
     * accessToken으로 카카오 사용자 정보 얻기
     */
    private JsonNode getNaverInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken); // 헤더에 accessToken 담아서 사용자 정보 요청
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.POST,
                naverUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(responseBody);

    }



}
