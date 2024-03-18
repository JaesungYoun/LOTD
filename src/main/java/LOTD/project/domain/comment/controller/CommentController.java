package LOTD.project.domain.comment.controller;

import LOTD.project.domain.comment.dto.request.CreateCommentRequest;
import LOTD.project.domain.comment.dto.request.UpdateCommentRequest;
import LOTD.project.domain.comment.dto.response.CreateCommentResponse;
import LOTD.project.domain.comment.dto.response.GetCommentListResponse;
import LOTD.project.domain.comment.service.CommentService;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentControllerDoc {

    private final CommentService commentService;

    @Override
    @PostMapping("/comments")
    public ResponseEntity<CreateCommentResponse> createComment(CreateCommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(request));
    }

    @Override
    @PutMapping("/comments")
    public void updateComment(UpdateCommentRequest request) {
        commentService.updateComment(request);
    }

    @Override
    @DeleteMapping("/comments")
    public void deleteComment(@RequestParam(name = "post_id") Long postId,
                              @RequestParam(name = "comment_id") Long commentId) {
        commentService.deleteComment(postId,commentId);
    }

    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> ExceptionHandle(BaseException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(e.getExceptionCode());
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }

}
