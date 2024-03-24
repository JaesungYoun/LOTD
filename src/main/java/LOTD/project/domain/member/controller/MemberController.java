package LOTD.project.domain.member.controller;

import LOTD.project.domain.member.Member;
import LOTD.project.domain.member.dto.request.MemberLoginRequest;
import LOTD.project.domain.member.dto.request.MemberSignUpRequest;
import LOTD.project.domain.member.dto.response.LoginResponse;
import LOTD.project.domain.member.dto.response.MemberSignUpResponse;
import LOTD.project.domain.member.service.MemberService;
import LOTD.project.global.exception.BaseException;
import LOTD.project.global.exception.ExceptionCode;
import LOTD.project.global.jwt.JwtService;
import LOTD.project.global.response.BaseResponse;
import LOTD.project.global.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


@RestController
@RequiredArgsConstructor
public class MemberController implements MemberControllerDoc{

    private final MemberService memberService;
    private final JwtService jwtService;
    private final BaseResponse baseResponse;

    /**
     * 회원가입 시 아이디 중복 여부 판단
     */
    @GetMapping("/memberid/check")
    public ResponseEntity<?> checkMemberId(@RequestParam String memberId) {
        boolean isDuplicated = false;
        isDuplicated = memberService.checkMemberId(memberId);
        if (isDuplicated == false) {
            return baseResponse.fail(HttpStatus.BAD_REQUEST,"이미 존재하는 아이디입니다. 다른 아이디를 입력 해주세요");
        }
        else{
            return baseResponse.success(HttpStatus.OK,"사용 가능한 아이디입니다.");
        }
    }


    /**
     * 회원가입 시 닉네임 중복 여부 판단
     */
    @GetMapping("/nicknames/check")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        boolean isDuplicated = false;
        isDuplicated = memberService.checkNickname(nickname);
        if (isDuplicated == false) {
            return baseResponse.fail(HttpStatus.BAD_REQUEST,"이미 존재하는 닉네임입니다. 다른 닉네임을 입력 해주세요");
        }
        else{
            return baseResponse.success(HttpStatus.OK,"사용 가능한 닉네임입니다.");
        }
    }



    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid MemberSignUpRequest memberSignUpRequest, BindingResult bindingResult) {

        try {
            if (!memberSignUpRequest.isMemberIdChecked() || !memberSignUpRequest.isNicknameChecked()) {
                return baseResponse.fail(HttpStatus.BAD_REQUEST, "아이디 또는 닉네임 중복확인을 해야합니다.");
            }

            // 유효성 검사를 통과하지 못한 경우 바로 에러 메시지 반환
            if (bindingResult.hasErrors()) {
                return baseResponse.fail(HttpStatus.BAD_REQUEST, "입력 항목을 올바르게 입력해주세요.");
            }
            if (!memberSignUpRequest.getPassword().equals(memberSignUpRequest.getConfirmPassword())) {
                throw new BaseException(ExceptionCode.NOT_SAME_PASSWORD);

            }
            Member member = memberService.signUp(memberSignUpRequest);

            return baseResponse.success(HttpStatus.OK, MemberSignUpResponse.of(member), "회원가입을 환영합니다!");

        } catch (BaseException e) {
            throw new BaseException(ExceptionCode.EXIST_MEMBER_ID);
        } catch (Exception e) {;
            return baseResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid MemberLoginRequest memberLoginRequest, BindingResult bindingResult) {

        // 유효성 검사를 통과하지 못한 경우 바로 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            return baseResponse.fail(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호를 올바르게 입력해주세요.");
        }

        Member member = memberService.findMember(memberLoginRequest.getMemberId());
        if (member != null) {
            LoginResponse loginResponse = memberService.login(member,memberLoginRequest);
            if (loginResponse != null) {
                return baseResponse.success(HttpStatus.OK, loginResponse, "로그인에 성공했습니다.");
            }
            else{
                return baseResponse.fail(HttpStatus.BAD_REQUEST,"비밀번호가 틀렸습니다.");
            }

        }
        else{
            return baseResponse.fail(HttpStatus.BAD_REQUEST,"존재하지 않는 회원입니다.");
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String accessToken = jwtService.getAccessToken(request);
        memberService.logout(accessToken);
        return baseResponse.success(HttpStatus.OK,"로그아웃 되었습니다.");
    }

    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> ExceptionHandle(BaseException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(e.getExceptionCode());
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }
}
