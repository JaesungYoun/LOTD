package LOTD.project.domain.member.controller;

import LOTD.project.domain.member.dto.request.*;
import LOTD.project.domain.member.dto.response.GetMyHeartPostListResponse;
import LOTD.project.domain.member.dto.response.MyPageResponse;
import LOTD.project.domain.member.service.ProfileService;
import LOTD.project.domain.post.controller.PostControllerDoc;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import LOTD.project.global.response.BaseResponse;
import LOTD.project.global.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ProfileController implements ProfileControllerDoc {


    private final ProfileService profileService;
    private final BaseResponse baseResponse;

    @Override
    @PutMapping("/members/nicknames")
    public ResponseEntity<?> updateMemberNickname(@RequestBody @Valid MemberUpdateNicknameRequest memberUpdateNicknameRequest ,BindingResult bindingResult,
                                                  @RequestParam(name = "member_id") String memberId) throws Exception {

        // 유효성 검사를 통과하지 못한 경우 바로 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            return baseResponse.fail(HttpStatus.BAD_REQUEST, "닉네임은 특수문자를 제외한 2~20자리로 입력해주세요");
        }

        profileService.updateMemberNickname(memberUpdateNicknameRequest, memberId);
        return baseResponse.success(HttpStatus.OK,"닉네임이 정상적으로 수정되었습니다.");
    }

    @Override
    @PutMapping("/members/emails")
    public ResponseEntity<?> updateMemberEmail(@RequestBody @Valid MemberUpdateEmailRequest memberUpdateEmailRequest, BindingResult bindingResult,
                                               @RequestParam(name = "member_id") String memberId) throws Exception {

        // 유효성 검사를 통과하지 못한 경우 바로 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            return baseResponse.fail(HttpStatus.BAD_REQUEST, "올바르지 않은 이메일 형식입니다. 이메일을 올바르게 입력해주세요. (최대 35자) ");
        }

        profileService.updateMemberEmail(memberUpdateEmailRequest, memberId);
        return baseResponse.success(HttpStatus.OK,"이메일이 정상적으로 수정되었습니다.");
    }

    @Override
    @PutMapping("/members/passwords")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest, BindingResult bindingResult,
                                            @RequestParam(name = "member_id") String memberId) throws Exception{

        String asIsPassword = changePasswordRequest.getAsIsPassword();
        String toBePassword = changePasswordRequest.getToBePassword();
        String confirmToBePassword = changePasswordRequest.getConfirmToBePassword();

        // 유효성 검사를 통과하지 못한 경우 바로 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            return baseResponse.fail(HttpStatus.BAD_REQUEST, "비밀번호는 8~20 자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다.");
        }

        if (!toBePassword.equals(confirmToBePassword)) {
            throw new BaseException(ExceptionCode.NOT_SAME_PASSWORD);
        }

        profileService.changePassword(changePasswordRequest.getAsIsPassword(), changePasswordRequest.getToBePassword(), memberId);
        return baseResponse.success(HttpStatus.OK,"비밀번호가 변경되었습니다.");
    }

    @Override
    @DeleteMapping("/members")
    public ResponseEntity<?> deleteMember(@RequestBody @Valid DeleteMemberRequest deleteMemberRequest, BindingResult bindingResult, @RequestParam(name = "member_id") String memberId) {

        // 유효성 검사를 통과하지 못한 경우 바로 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            return baseResponse.fail(HttpStatus.BAD_REQUEST, "비밀번호는 8~20 자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다.");
        }

        profileService.deleteMember(deleteMemberRequest.getPassword(),memberId);
        return baseResponse.success(HttpStatus.OK,"탈퇴 완료되었습니다.");
    }

    @Override
    @DeleteMapping("/social-members")
    public ResponseEntity<?> deleteSocialMember(@RequestParam(name = "member_id") String memberId) {

        profileService.deleteSocialMember(memberId);
        return baseResponse.success(HttpStatus.OK,"탈퇴 완료되었습니다.");
    }

    @Override
    @GetMapping("/members")
    public ResponseEntity<?> MyPage(@RequestParam(name = "member_id") String memberId) {
        MyPageResponse myPageResponse = profileService.myPage(memberId);
        return baseResponse.success(HttpStatus.OK,myPageResponse,"내 정보 조회 완료");
    }

    @Override
    @GetMapping("/members/hearts")
    public ResponseEntity<GetMyHeartPostListResponse> getMyHeartPostList(@RequestParam(name = "member_id") String memberId,
                                                                         @PageableDefault(page = 0, size = 10, sort = "createDateTime",direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(profileService.getMyHeartPostList(memberId,pageable));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> ExceptionHandle(BaseException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(e.getExceptionCode());
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }

}
