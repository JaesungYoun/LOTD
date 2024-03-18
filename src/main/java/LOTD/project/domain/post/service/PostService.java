package LOTD.project.domain.post.service;

import LOTD.project.domain.category.Category;
import LOTD.project.domain.category.repository.CategoryRepository;
import LOTD.project.domain.comment.dto.response.GetCommentListResponse;
import LOTD.project.domain.heart.Heart;
import LOTD.project.domain.heart.repository.HeartRepository;
import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.repository.MemberRepository;
import LOTD.project.domain.post.Post;
import LOTD.project.domain.post.dto.request.CreatePostRequest;
import LOTD.project.domain.post.dto.request.UpdatePostRequest;
import LOTD.project.domain.post.dto.response.CreatePostResponse;
import LOTD.project.domain.post.dto.response.GetBoardResponse;
import LOTD.project.domain.post.dto.response.GetPostResponse;
import LOTD.project.domain.post.repository.PostRepository;
import LOTD.project.domain.post.repository.PostRepositoryCustom;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import LOTD.project.global.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final HeartRepository heartRepository;
    private final PostRepositoryCustom postRepositoryCustom;

    @Transactional(readOnly = true)
    public GetBoardResponse getBoardList(String searchType, String text, Pageable pageable) {
        Page<Post> postList;

        if (StringUtil.isEmpty(searchType)) {
            postList = postRepository.findAll(pageable);
        }
        else {
            if (searchType.equals("creator")) {
                postList = postRepository.findAllByCreatorContains(text, pageable);
            }
            else if (searchType.equals("content")){
                postList = postRepository.findAllByContentContains(text, pageable);
            }
            else {
                postList = postRepository.findAllByTitleContains(text, pageable);
            }

        }

        List<GetBoardResponse.InnerGetBoard> response = new ArrayList<>();

        response = postList.stream()
                .map(data -> GetBoardResponse.InnerGetBoard.builder()
                        .postId(data.getPostId())
                        .categoryId(data.getCategory().getCategoryId())
                        .title(data.getTitle())
                        .commentsCount(data.getCommentsCount())
                        .hits(data.getHits())
                        .totalPages(postList.getTotalPages())
                        .totalElements(postList.getTotalElements())
                        .creator(data.getMember().getNickname())
                        .createdDateTime(data.getCreateDateTime())
                        .build())
                .collect(Collectors.toList());

        return GetBoardResponse.builder().getBoardListList(response).build();

    }

    @Transactional
    public GetPostResponse getPost(Long postId, Long categoryId, String requestMemberId) {

        Post post = postRepository.findByPostId(postId)
                .orElseThrow(()-> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        if (post.getCategory().getCategoryId() != categoryId) {
            throw new BaseException(ExceptionCode.WRONG_REQUEST_DATA);
        }


        Member requestMember = memberRepository.findByMemberId(requestMemberId)
                .orElseThrow(()-> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        // 좋아요 여부 체크
        Heart heart = heartRepository.findByMemberAndPost(requestMember,post).orElse(null);
        String heartYn = "N";

        // 좋아요가 존재하면 좋아요 여부 Y 설정
        if (heart != null) {
            heartYn = "Y";
        }

        // 조회 수 증가
        postRepositoryCustom.increaseHitsCount(post);

        return GetPostResponse.builder()
                .categoryId(post.getCategory().getCategoryId())
                .postId(post.getPostId())
                .memberId(post.getMember().getMemberId())
                .title(post.getTitle())
                .content(post.getContent())
                .image(post.getImage())
                .commentsCount(post.getCommentsCount())
                .heartCount(post.getHeartCount())
                .heartYn(heartYn)
                .hits(post.getHits())
                .creator(post.getMember().getNickname())
                .createdDate(post.getCreateDateTime())
                .commentList(GetCommentListResponse.builder().commentList(post.getComment().stream()
                                .map(data -> GetCommentListResponse.InnerComment.builder()
                                        .memberId(data.getMember().getMemberId())
                                        .creator(data.getMember().getNickname())
                                        .commentId(data.getCommentId())
                                        .parentCommentId(data.getParentCommentId())
                                        .content(data.getContent())
                                        .createdDate(data.getCreateDateTime())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .build();


        //.commentList(post.getComment().stream().map(GetCommentListResponse::new).collect(Collectors.toList()))
        //.build();

    }

    @Transactional
    public CreatePostResponse createPost(CreatePostRequest request) {


        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Category category = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        Post post = Post.builder()
                .category(category)
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .image(request.getImage())
                .build();

        postRepository.save(post);

        return CreatePostResponse.builder()
                .categoryId(post.getCategory().getCategoryId())
                .postId(post.getPostId())
                .title(post.getTitle())
                .build();

    }

    @Transactional
    public void updatePost(Long postId, UpdatePostRequest request) {
        Post post = postRepository.findByPostId(postId).orElseThrow(()-> new BaseException(ExceptionCode.DATA_NOT_FOUND));

        post.updatePost(request.getTitle(), request.getContent(), request.getImage());
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findByPostId(postId).orElseThrow(()-> new BaseException(ExceptionCode.DATA_NOT_FOUND));
        postRepository.delete(post);
    }


}
