package LOTD.project.domain.comment.controller;

import LOTD.project.domain.comment.dto.request.CreateCommentRequest;
import LOTD.project.domain.comment.dto.request.UpdateCommentRequest;
import LOTD.project.domain.comment.dto.response.CreateCommentResponse;
import LOTD.project.domain.comment.dto.response.GetCommentListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Tag(name = "댓글 API")
public interface CommentControllerDoc {

    @Operation(summary = "댓글 생성", description = "댓글을 생성합니다.")
    ResponseEntity<CreateCommentResponse> createComment(@RequestBody @Valid CreateCommentRequest request);

    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
    void updateComment(@RequestBody @Valid UpdateCommentRequest request);

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    void deleteComment(@RequestParam(name = "post_id") Long postId,
                       @RequestParam(name = "comment_id") Long commentId);

}
