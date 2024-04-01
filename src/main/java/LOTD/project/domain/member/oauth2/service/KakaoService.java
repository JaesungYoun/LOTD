package LOTD.project.domain.member.oauth2.service;

import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.Role;
import LOTD.project.domain.member.dto.response.LoginResponse;
import LOTD.project.domain.member.oauth2.SocialType;
import LOTD.project.domain.member.oauth2.dto.request.KakaoSignUpRequest;
import LOTD.project.domain.member.oauth2.dto.response.KakaoInfo;
import LOTD.project.domain.member.oauth2.dto.response.KakaoToken;
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

@RequiredArgsConstructor
@Transactional
@Service
public class KakaoService {

    @Value("${oauth.kakao.client_id}")
    String clientId;
    @Value("${oauth.kakao.redirect_uri}")
    String redirectUri;
    @Value("${oauth.kakao.client_secret}")
    String clientSecret;


    private final MemberRepository memberRepository;
    private final JwtService jwtService;



    /**
     * 카카오 회원가입 및 로그인 (첫 로그인시만)
     * @param
     * @return
     * @throws JsonProcessingException
     */
    public LoginResponse kakaoSignUpAndLogin(KakaoSignUpRequest kakaoSignUpRequest, HttpServletResponse response) {

        Member member = Member.builder()
                .memberId(kakaoSignUpRequest.getKakaoMemberId())
                .password(UUID.randomUUID().toString())
                .nickname(kakaoSignUpRequest.getNickname())
                .email(kakaoSignUpRequest.getEmail())
                .role(Role.MEMBER)
                .socialType(SocialType.KAKAO)
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
                .socialType(SocialType.KAKAO)
                .build();
    }

    /**
     * 카카오 로그인 (우선 로그인 버튼 눌리면 인가 코드 받아서 토큰까지 발급, 그리고 기존 회원인지, 아닌지 구분
     * @param code
     * @param response
     * @return
     * @throws JsonProcessingException
     */


    public LoginResponse kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        KakaoToken kakaoToken = getKakaoToken(code);

        String accessToken = kakaoToken.getAccessToken();

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
                    .socialType(SocialType.KAKAO)
                    .build();
        }
    }


    public KakaoInfo getMemberInfoToSend(String accessToken) throws JsonProcessingException {
        JsonNode jsonNode = getKakaoInfo(accessToken);

        // DB 에 중복된 Kakao Id 가 있는지 확인
        String kakaoId = String.valueOf(jsonNode.get("id").asLong()) + "@k";

        String email = null;
        if (jsonNode.get("kakao_account").get("email") != null) {
            email = jsonNode.get("kakao_account").get("email").asText();
            }
        return KakaoInfo.builder()
                .kakaoMemberId(kakaoId)
                .email(email)
                .build();
    }

    private Member getMember(String accessToken) throws JsonProcessingException {
        JsonNode jsonNode = getKakaoInfo(accessToken);

        // DB 에 중복된 Kakao Id 가 있는지 확인
        String kakaoId = String.valueOf(jsonNode.get("id").asLong()) + "@k";
        Member kakaoMember = memberRepository.findByMemberId(kakaoId).orElse(null);

        if (kakaoMember == null) {
            return null;
        }
        return kakaoMember;
    }

     private KakaoToken getKakaoToken(String code) throws JsonProcessingException {
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
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String tokenType = jsonNode != null ? jsonNode.get("token_type").asText() : null;
        String accessToken = jsonNode != null ? jsonNode.get("access_token").asText() : null;
        Integer expiresIn = jsonNode != null ? jsonNode.get("expires_in").asInt() : null;
        String refreshToken = jsonNode != null ? jsonNode.get("refresh_token").asText() : null;
        Integer refreshTokenExpiresIn = jsonNode != null ? jsonNode.get("refresh_token_expires_in").asInt() : null;
        String scope = jsonNode != null ? jsonNode.get("scope").asText() : null;

        KakaoToken kakaoToken = KakaoToken.builder()
                .tokenType(tokenType)
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn(refreshTokenExpiresIn)
                .scope(scope)
                .build();

        return kakaoToken;
    }

    /**
     * accessToken으로 카카오 사용자 정보 얻기
     */
    private JsonNode getKakaoInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(responseBody);

    }


}
