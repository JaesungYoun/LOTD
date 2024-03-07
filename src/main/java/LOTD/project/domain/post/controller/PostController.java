package LOTD.project.domain.post.controller;

import LOTD.project.domain.post.dto.request.CreatePostRequest;
import LOTD.project.domain.post.dto.request.DeletePostRequest;
import LOTD.project.domain.post.dto.request.UpdatePostRequest;
import LOTD.project.domain.post.dto.response.CreatePostResponse;
import LOTD.project.domain.post.dto.response.GetBoardResponse;
import LOTD.project.domain.post.dto.response.GetPostResponse;
import LOTD.project.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


    @GetMapping("/boards")
    public ResponseEntity<GetBoardResponse> getBoardList(@RequestParam(name = "searchType") String searchType,
                                                         @RequestParam(name = "text") String text) {
        return ResponseEntity.ok(postService.getBoardList(searchType,text));
    }

    @GetMapping("/posts")
    public ResponseEntity<GetPostResponse> getPost(@RequestParam(name = "post_id") Long postId,
                                                   @RequestParam(name = "category_id") Long categoryId) {

        return ResponseEntity.ok(postService.getPost(postId,categoryId));
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





}

