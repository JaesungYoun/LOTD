package LOTD.project.domain.member.controller;

import LOTD.project.domain.member.dto.request.ChangePasswordRequest;
import LOTD.project.domain.member.dto.request.DeleteMemberRequest;
import LOTD.project.domain.member.dto.request.MemberUpdateEmailRequest;
import LOTD.project.domain.member.dto.request.MemberUpdateNicknameRequest;
import LOTD.project.domain.member.dto.response.GetMyHeartPostListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Tag(name = "프로필(마이페이지) API")
public interface ProfileControllerDoc {


    @Operation(summary = "회원 닉네임 수정", description = "회원 닉네임을 수정합니다.")
    ResponseEntity<?> updateMemberNickname(@RequestBody @Valid MemberUpdateNicknameRequest memberUpdateNicknameRequest , BindingResult bindingResult,
                                           @RequestParam(name = "member_id") String memberId) throws Exception;

    @Operation(summary = "회원 이메일 수정", description = "회원 이메일을 수정합니다.")
    ResponseEntity<?> updateMemberEmail(@RequestBody @Valid MemberUpdateEmailRequest memberUpdateEmailRequest, BindingResult bindingResult,
                                        @RequestParam(name = "member_id") String memberId) throws Exception;

    @Operation(summary = "회원 비밀번호 변경", description = "회원 비밀번호를 변경합니다.")
    ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest, BindingResult bindingResult,
                                     @RequestParam(name = "member_id") String memberId) throws Exception;

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
    ResponseEntity<?> deleteMember(@RequestBody @Valid DeleteMemberRequest deleteMemberRequest, BindingResult bindingResult,
                                   @RequestParam(name = "member_id") String memberId);

    @Operation(summary = "소셜 플랫폼 회원 탈퇴", description = "소셜 플랫폼 회원 탈퇴를 진행합니다.")
    ResponseEntity<?> deleteSocialMember(@RequestParam(name = "member_id") String memberId);

    @Operation(summary = "회원정보 조회", description = "회원정보를 조회를 진행합니다.")
    ResponseEntity<?> MyPage(@RequestParam(name = "member_id") String memberId);

    @Operation(summary = "좋아요 글 목록 조회", description = "좋아요를 한 글 목록을 조회합니다.")
    ResponseEntity<GetMyHeartPostListResponse> getMyHeartPostList(@RequestParam(name = "member_id") String memberId,
                                                                  @PageableDefault(page = 0, size = 10, sort = "createDateTime",direction = Sort.Direction.DESC) Pageable pageable);
    

}
