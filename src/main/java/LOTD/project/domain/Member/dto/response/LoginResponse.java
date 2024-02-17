package LOTD.project.domain.Member.dto.response;

import LOTD.project.domain.Member.oauth2.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LoginResponse {

        private Long id;
        private String grantType;
        private String accessToken;
        private String refreshToken;
        private Long accessTokenExpiresIn;
        private Long refreshTokenExpiresIn;
        private SocialType socialType;

}
