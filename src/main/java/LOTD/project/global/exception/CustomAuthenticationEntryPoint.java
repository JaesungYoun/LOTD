package LOTD.project.global.exception;

import LOTD.project.global.response.ExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {



        BaseException baseException = (BaseException) request.getAttribute("exception");

        /*
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> data = new HashMap<>();
*/
        if (baseException != null) {
            // 토큰 만료의 경우 다른 응답
            if (baseException.getExceptionCode().getErrorCode().equals("TOKEN_01")) {
                setResponse(response, ExceptionCode.TOKEN_EXPIRED);
                return;
            }

            // 유효한 토큰이 아닌 경우 다른 응답
            if (baseException.getExceptionCode().getErrorCode().equals("TOKEN_02")) {
                setResponse(response, ExceptionCode.NOT_VALID_TOKEN);
                return;
            }

            if (baseException.getExceptionCode().getErrorCode().equals("TOKEN_03")) {
                setResponse(response, ExceptionCode.UNSUPPORTED_TOKEN);
                return;
            }

            if (baseException.getExceptionCode().getErrorCode().equals("TOKEN_04")) {
                setResponse(response, ExceptionCode.NOT_ACCESS_TOKEN_TYPE);
                return;
            }

            if (baseException.getExceptionCode().getErrorCode().equals("TOKEN_05")) {
                setResponse(response, ExceptionCode.BLACKLISTED_ACCESS_TOKEN);
                return;
            }

            if (baseException.getExceptionCode().getErrorCode().equals("TOKEN_06")) {
                setResponse(response, ExceptionCode.ALL_TOKEN_EXPIRED);
                return;
            }

            if (baseException.getExceptionCode().getErrorCode().equals("MEMBER_04")) {
                setResponse(response, ExceptionCode.NOT_EXIST_MEMBER);
                return;
            }
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

    }


    //한글 출력을 위해 getWriter() 사용
    private void setResponse(HttpServletResponse response, ExceptionCode exceptionCode) throws IOException {


        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionCode);

        String body = objectMapper.writeValueAsString(exceptionResponse);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body);
    }


}