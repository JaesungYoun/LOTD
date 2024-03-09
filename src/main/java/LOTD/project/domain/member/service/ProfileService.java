package LOTD.project.domain.member.service;

import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.dto.request.MemberUpdateEmailRequest;
import LOTD.project.domain.member.dto.request.MemberUpdateNicknameRequest;
import LOTD.project.domain.member.dto.response.MyPageResponse;
import LOTD.project.domain.member.repository.MemberRepository;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import LOTD.project.global.login.service.redis.RedisService;
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
    private final RedisService redisService;
    /**
     * 회원정보 수정 ( 닉네임 ,나이 등 )
     * @param
     */
    public void updateMemberNickname(MemberUpdateNicknameRequest memberUpdateNicknameRequest, String memberId){
        Member member = memberRepository.findByMemberId(memberId).orElse(null);

        if (memberUpdateNicknameRequest.getNickname() != null){
            member.updateNickname(memberUpdateNicknameRequest.getNickname());
        }

    }

    public void updateMemberEmail(MemberUpdateEmailRequest memberUpdateEmailRequest, String memberId){
        Member member = memberRepository.findByMemberId(memberId).orElse(null);

        if (memberUpdateEmailRequest.getEmail() != null){
            member.updateEmail(memberUpdateEmailRequest.getEmail());
        }

    }

    /**
     * 비밀번호 변경
     * @param asIsPassword
     * @param toBePassword
     */
    public void changePassword(String asIsPassword, String toBePassword, String memberId) {
        Member member = memberRepository.findByMemberId(memberId).orElse(null);

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

    public void deleteMember(String checkPassword, String memberId){
        Member member = memberRepository.findByMemberId(memberId).orElse(null);

        if (member != null) {
            if (!member.matchPassword(passwordEncoder, checkPassword)) {
                throw new BaseException(ExceptionCode.WRONG_PASSWORD);
            }

            // RefreshToken을 Redis에서 삭제
            redisService.delRefreshToken(member.getMemberId());
            
            // 회원 DB에서 삭제
            memberRepository.delete(member);


        }

    }

    public void deleteSocialMember(String memberId){
        Member member = memberRepository.findByMemberId(memberId).orElse(null);

        if (member != null) {

            // RefreshToken을 Redis에서 삭제
            redisService.delRefreshToken(member.getMemberId());

            // 회원 DB에서 삭제
            memberRepository.delete(member);

        } else {
            throw new BaseException(ExceptionCode.NOT_EXIST_MEMBER);
        }

    }


    public MyPageResponse myPage(String memberId){
        Member member = memberRepository.findByMemberId(memberId).orElse(null);

        if (member == null) {
            throw new BaseException(ExceptionCode.NOT_EXIST_MEMBER);
        }
        else {
            return MyPageResponse.builder()
                    .memberId(member.getMemberId())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .socialType(member.getSocialType())
                    .build();
        }
    }

}
