package com.dalbitlive.security.handler;

import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.json.CustomObjectMapper;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.exception.CustomUsernameNotFoundException;
import com.dalbitlive.util.DalbitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component("authFailureHandler")
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        DalbitUtil.setHeader(request, response);
        Status status = ((CustomUsernameNotFoundException) exception).getStatus();
        Object data = ((CustomUsernameNotFoundException) exception).getData();
        PrintWriter out = response.getWriter();
        CustomObjectMapper mapper = new CustomObjectMapper();
        out.print(mapper.writeValueAsString(new JsonOutputVo(status, data)));
        out.flush();
        out.close();
    }
}
