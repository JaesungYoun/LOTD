package LOTD.project.domain.post.controller;

import LOTD.project.domain.post.dto.request.CreatePostRequest;
import LOTD.project.domain.post.dto.request.DeletePostRequest;
import LOTD.project.domain.post.dto.request.UpdatePostRequest;
import LOTD.project.domain.post.dto.response.CreatePostResponse;
import LOTD.project.domain.post.dto.response.GetBoardResponse;
import LOTD.project.domain.post.dto.response.GetPostResponse;
import LOTD.project.domain.post.service.PostService;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class PostController implements PostControllerDoc{

    private final PostService postService;


    @GetMapping("/boards")
    public ResponseEntity<GetBoardResponse> getBoardList(@RequestParam(name = "search_type") String searchType,
                                                         @RequestParam(name = "text", required = false) String text,
                                                         @PageableDefault(page = 0, size = 10, sort = "createDateTime",direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getBoardList(searchType, text, pageable));
    }

    @GetMapping("/posts")
    public ResponseEntity<GetPostResponse> getPost(@RequestParam(name = "post_id") Long postId,
                                                   @RequestParam(name = "category_id") Long categoryId,
                                                   @RequestParam(name = "request_member_id") String requestMemberId) {

        return ResponseEntity.ok(postService.getPost(postId,categoryId,requestMemberId));
    }

    @PostMapping("/posts")
    public ResponseEntity<CreatePostResponse> createPost(@RequestBody @Valid CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @PutMapping("/posts")
    public void updatePost(@RequestParam(name = "post_id") Long postId, @RequestBody @Valid UpdatePostRequest request) {
        postService.updatePost(postId,request);

    }

    @DeleteMapping("/posts")
    public void deletePost(@RequestParam(name = "post_id") Long postId) {
        postService.deletePost(postId);
    }


    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> ExceptionHandle(BaseException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(e.getExceptionCode());
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }



}

