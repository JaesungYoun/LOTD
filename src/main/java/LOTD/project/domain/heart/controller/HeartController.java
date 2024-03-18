package LOTD.project.domain.heart.controller;

import LOTD.project.domain.heart.dto.request.HeartClickRequest;
import LOTD.project.domain.heart.service.HeartService;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class HeartController implements HeartControllerDoc{


    private final HeartService heartService;
    @Override
    @PostMapping("/hearts")
    public void heartClick(HeartClickRequest request) {
        heartService.heartClick(request);
    }

    @Override
    @DeleteMapping("/hearts")
    public void heartCancel(@RequestParam(name = "memberId") String memberId,
                            @RequestParam(name = "postId") Long postId) {
        heartService.heartCancel(memberId,postId);

    }

    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> ExceptionHandle(BaseException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(e.getExceptionCode());
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }


}
