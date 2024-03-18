package LOTD.project.domain.heart.service;

import LOTD.project.domain.heart.Heart;
import LOTD.project.domain.heart.dto.request.HeartClickRequest;
import LOTD.project.domain.heart.repository.HeartRepository;
import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.repository.MemberRepository;
import LOTD.project.domain.post.Post;
import LOTD.project.domain.post.repository.PostRepository;
import LOTD.project.domain.post.repository.PostRepositoryCustom;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HeartService {

    private final HeartRepository heartRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostRepositoryCustom postRepositoryCustom;

    @Transactional
    public void heartClick(HeartClickRequest request) {
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Post post = postRepository.findByPostId(request.getPostId())
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        heartRepository.findByMemberAndPost(member,post)
                .ifPresent(m -> {
                    throw new BaseException(ExceptionCode.DATA_ALREADY_EXISTS);
                });


        Heart heart = Heart.builder()
                .member(member)
                .post(post)
                .build();

        // 좋아요 등록
        heartRepository.save(heart);

        // 게시글 좋아요 수 증가
        postRepositoryCustom.increaseHeartCount(post);

    }

    @Transactional
    public void heartCancel(String memberId, Long postId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Heart heart = heartRepository.findByMemberAndPost(member,post)
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));


        // 좋아요 취소
        heartRepository.delete(heart);
        
        // 게시글 좋아요 수 감소
        postRepositoryCustom.reduceHeartCount(post);

    }


}
