package LOTD.project.domain.comment.service;

import LOTD.project.domain.comment.Comment;
import LOTD.project.domain.comment.dto.request.CreateCommentRequest;
import LOTD.project.domain.comment.dto.request.UpdateCommentRequest;
import LOTD.project.domain.comment.dto.response.CreateCommentResponse;
import LOTD.project.domain.comment.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostRepositoryCustom postRepositoryCustom;

    @Transactional
    public CreateCommentResponse createComment(CreateCommentRequest request) {

        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Post post = postRepository.findByPostId(request.getPostId())
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
                .parentCommentId(request.getParentCommentId())
                .content(request.getContent())
                .build();

        // 댓글 저장
        commentRepository.save(comment);

        // 댓글 수 증가
        postRepositoryCustom.increaseCommentCount(post);

        return CreateCommentResponse.builder().commentId(comment.getCommentId()).build();

    }

    @Transactional
    public void updateComment(UpdateCommentRequest request) {

        Comment comment = commentRepository.findByCommentId(request.getCommentId())
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));


        // 댓글 수정
        comment.updateComment(request.getContent());

        // 댓글 저장
        commentRepository.save(comment);

    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Comment comment = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        // 댓글 삭제
        commentRepository.delete(comment);

        // 댓글 수 감소
        postRepositoryCustom.reduceCommentCount(post);

    }

}
