package LOTD.project.domain.member.service;

import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.dto.request.MemberUpdateEmailRequest;
import LOTD.project.domain.member.dto.request.MemberUpdateNicknameRequest;
import LOTD.project.domain.member.dto.response.GetMyCommentPostListResponse;
import LOTD.project.domain.member.dto.response.GetMyHeartPostListResponse;
import LOTD.project.domain.member.dto.response.MyPageResponse;
import LOTD.project.domain.member.repository.MemberRepository;
import LOTD.project.domain.post.repository.PostRepositoryCustom;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import LOTD.project.global.login.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final PostRepositoryCustom postRepositoryCustom;
    /**
     * 회원정보 수정 ( 닉네임 ,나이 등 )
     * @param
     */
    @Transactional(readOnly = true)
    public void updateMemberNickname(MemberUpdateNicknameRequest memberUpdateNicknameRequest, String memberId){
        Member member = memberRepository.findByMemberId(memberId).orElse(null);

        if (memberUpdateNicknameRequest.getNickname() != null){
            member.updateNickname(memberUpdateNicknameRequest.getNickname());
        }

    }
    @Transactional
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
    @Transactional
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

    @Transactional
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

    @Transactional
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


    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public GetMyHeartPostListResponse getMyHeartPostList(String memberId, Pageable pageable) {
        Page<GetMyHeartPostListResponse.InnerGetMyHeartPost> heartPost = postRepositoryCustom
                .getMyHeartPostList(memberId, pageable);

        List<GetMyHeartPostListResponse.InnerGetMyHeartPost> response = new ArrayList<>(); // null 방지

        response = heartPost.stream()
                .map(data -> GetMyHeartPostListResponse.InnerGetMyHeartPost.builder()
                        .postId(data.getPostId())
                        .categoryId(data.getCategoryId())
                        .title(data.getTitle())
                        .commentCount(data.getCommentCount())
                        .hits(data.getHits())
                        .createDateTime(data.getCreateDateTime())
                        .totalPages(heartPost.getTotalPages())
                        .totalElements(heartPost.getTotalElements())
                        .build())
                .collect(Collectors.toList());

        return GetMyHeartPostListResponse.builder().heartPostList(response).build();

    }

    @Transactional(readOnly = true)
    public GetMyCommentPostListResponse getMyCommentPostList(String memberId, Pageable pageable) {
        Page<GetMyCommentPostListResponse.InnerGetMyCommentPost> commentPost = postRepositoryCustom
                .getMyCommentPostList(memberId,pageable);

        List<GetMyCommentPostListResponse.InnerGetMyCommentPost> response = new ArrayList<>(); // null 방지

        response = commentPost.stream()
                .map(data -> GetMyCommentPostListResponse.InnerGetMyCommentPost.builder()
                        .postId(data.getPostId())
                        .categoryId(data.getCategoryId())
                        .postTitle(data.getPostTitle())
                        .commentContent(data.getCommentContent())
                        .commentCount(data.getCommentCount())
                        .postHits(data.getPostHits())
                        .createDateTime(data.getCreateDateTime())
                        .totalPages(commentPost.getTotalPages())
                        .totalElements(commentPost.getTotalElements())
                        .build())
                .collect(Collectors.toList());


        return GetMyCommentPostListResponse.builder().commentsPostList(response).build();
    }




}
