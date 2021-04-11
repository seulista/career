package com.dalbitlive.security.handler;

import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.json.CustomObjectMapper;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.member.vo.TokenVo;
import com.dalbitlive.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component("logoutSuccessHandler")
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Autowired
    private LoginUtil loginUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try{
            PrintWriter out = response.getWriter();
            CustomObjectMapper mapper = new CustomObjectMapper();
            out.print(mapper.writeValueAsString(new JsonOutputVo(Status.로그아웃성공, loginUtil.getTokenVo(request))));
            out.flush();
            out.close();
        }catch(IOException e){}
    }
}
