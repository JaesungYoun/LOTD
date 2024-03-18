package LOTD.project.domain.heart.controller;

import LOTD.project.domain.heart.dto.request.HeartClickRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "좋아요 API")
public interface HeartControllerDoc {

    @Operation(summary = "좋아요" , description = "좋아요.")
    void heartClick(HeartClickRequest request);

    @Operation(summary = "좋아요 취소" , description = "좋아요를 취소합니다.")
    void heartCancel (@RequestParam(name = "memberId") String memberId,
                      @RequestParam(name = "postId") Long postId);

}
