package LOTD.project.domain.member.dto.response;

import LOTD.project.domain.member.oauth2.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyPageResponse {

    private String memberId;
    private String nickname;
    private String email;
    private SocialType socialType;

}
