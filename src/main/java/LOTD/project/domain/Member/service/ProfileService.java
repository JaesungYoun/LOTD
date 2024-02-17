package LOTD.project.domain.Member.service;

import LOTD.project.domain.Member.Member;
import LOTD.project.domain.Member.dto.request.MemberUpdateAgeRequest;
import LOTD.project.domain.Member.dto.request.MemberUpdateEmailRequest;
import LOTD.project.domain.Member.dto.request.MemberUpdateNickNameRequest;
import LOTD.project.domain.Member.dto.response.MyPageResponse;
import LOTD.project.domain.Member.repository.MemberRepository;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원정보 수정 ( 닉네임 ,나이 등 )
     * @param memberUpdateNickNameRequest
     */
    public void updateMemberNickName(MemberUpdateNickNameRequest memberUpdateNickNameRequest, Long id){
        Member member = memberRepository.findById(id).orElse(null);

        if (memberUpdateNickNameRequest.getNickName() != null){
            member.updateNickname(memberUpdateNickNameRequest.getNickName());
        }

    }

    public void updateMemberAge(MemberUpdateAgeRequest memberUpdateAgeRequest, Long id){
        Member member = memberRepository.findById(id).orElse(null);

        if (memberUpdateAgeRequest.getAge() != 0){
            member.updateAge(memberUpdateAgeRequest.getAge());
        }

    }

    public void updateMemberEmail(MemberUpdateEmailRequest memberUpdateEmailRequest, Long id){
        Member member = memberRepository.findById(id).orElse(null);

        if (memberUpdateEmailRequest.getEmail() != null){
            member.updateEmail(memberUpdateEmailRequest.getEmail());
        }

    }

    /**
     * 비밀번호 변경
     * @param asIsPassword
     * @param toBePassword
     */
    public void changePassword(String asIsPassword, String toBePassword, Long id) {
        Member member = memberRepository.findById(id).orElse(null);

        if (member != null) {
            if (!member.matchPassword(passwordEncoder,asIsPassword)){
                throw new BaseException(ExceptionCode.WRONG_PASSWORD);
            }
            member.changePassword(passwordEncoder,toBePassword);
        }
    }

    /**
     * 회원 탈퇴
     */

    public void delMember(String checkPassword, Long id){
        Member member = memberRepository.findById(id).orElse(null);

        if (member != null) {
            if (!member.matchPassword(passwordEncoder, checkPassword)) {
                throw new BaseException(ExceptionCode.WRONG_PASSWORD);
            }
            memberRepository.delete(member);
        }

    }

    public MyPageResponse myPage(Long id){
        Member member = memberRepository.findById(id).orElse(null);

        if (member == null) {
            throw new BaseException(ExceptionCode.NOT_LOGIN);
        }
        else {
            return MyPageResponse.builder()
                    .memberId(member.getMemberId())
                    .nickName(member.getNickName())
                    .age(member.getAge())
                    .build();
        }
    }

}
