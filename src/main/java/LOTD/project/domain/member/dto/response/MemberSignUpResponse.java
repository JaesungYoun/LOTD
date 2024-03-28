package LOTD.project.domain.member.dto.response;

import LOTD.project.domain.member.Member;
import LOTD.project.global.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignUpResponse extends BaseResponse {

    private String memberId;
    private String nickname;
    private String email;

    public static MemberSignUpResponse of(Member member) {

        MemberSignUpResponse memberSignUpResponse = new MemberSignUpResponse();
        memberSignUpResponse.setMemberId(member.getMemberId());
        memberSignUpResponse.setNickname(member.getNickname());
        memberSignUpResponse.setEmail(member.getEmail());

        return memberSignUpResponse;
    }
}
