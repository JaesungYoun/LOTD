package LOTD.project.domain.post.controller;

import LOTD.project.domain.post.dto.request.CreatePostRequest;
import LOTD.project.domain.post.dto.request.UpdatePostRequest;
import LOTD.project.domain.post.dto.response.CreatePostResponse;
import LOTD.project.domain.post.dto.response.GetBoardResponse;
import LOTD.project.domain.post.dto.response.GetPostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "게시글 API")
public interface PostControllerDoc {


    @Operation(summary = "게시글 목록 조회" , description = "게시글 목록을 조회합니다.")
    ResponseEntity<GetBoardResponse> getBoardList(@RequestParam(name = "search_type") String searchType,
                                                  @RequestParam(name = "text", required = false) String text,
                                                  @PageableDefault(page = 0, size = 10, sort = "createDateTime") Pageable pageable);

    @Operation(summary = "게시글 조회" , description = "게시글을 조회합니다.")
    ResponseEntity<GetPostResponse> getPost(@RequestParam(name = "post_id") Long postId,
                                            @RequestParam(name = "category_id") Long categoryId,
                                            @RequestParam(name = "request_member_id") String requestMemberId);

    @Operation(summary = "게시글 생성" , description = "게시글을 생성합니다.")
    ResponseEntity<CreatePostResponse> createPost(@RequestBody @Valid CreatePostRequest request);

    @Operation(summary = "게시글 수정" , description = "게시글을 수정합니다.")
    void updatePost(@RequestParam(name = "post_id") Long postId, @RequestBody @Valid UpdatePostRequest request);

    @Operation(summary = "게시글 삭제" , description = "게시글을 삭제합니다.")
    public void deletePost(@RequestParam(name = "post_id") Long postId);


}
