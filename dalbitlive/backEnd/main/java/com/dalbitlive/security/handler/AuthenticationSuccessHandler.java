package com.dalbitlive.security.handler;

import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.json.CustomObjectMapper;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.util.DalbitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component("authSuccessHandler")
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${session.name}")
    private String SESSION_NAME;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_NAME, authentication.getDetails());

        DalbitUtil.setHeader(request, response);
        PrintWriter out = response.getWriter();
        CustomObjectMapper mapper = new CustomObjectMapper();
        out.print(mapper.writeValueAsString(new JsonOutputVo(Status.로그인성공, authentication.getDetails())));
        out.flush();
        out.close();
    }
}
