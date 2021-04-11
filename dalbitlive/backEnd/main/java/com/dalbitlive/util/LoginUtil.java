package com.dalbitlive.util;

import com.dalbitlive.exception.GlobalException;
import com.dalbitlive.member.vo.TokenVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Component
@Scope("prototype")
public class LoginUtil {
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${sso.header.cookie.name}")
    private String TOKEN_HEADER_NAME;
    @Value("${session.name}")
    private String SESSION_NAME;

    private TokenVo tokenVo;

    public void init(HttpServletRequest request) {
        if(tokenVo == null){
            HttpSession session = request.getSession(true);
            tokenVo = (TokenVo)session.getAttribute(SESSION_NAME);
            log.debug("Session token {}", tokenVo == null);
            if(tokenVo == null){
                Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
                if(details instanceof TokenVo){
                    tokenVo = (TokenVo)details;
                }
                log.debug("SecurityContextHolder token {}", tokenVo == null);
                if(tokenVo == null){
                    if(!request.getRequestURI().startsWith("/member/login")) {
                        try {
                            tokenVo = jwtUtil.getTokenVoFromJwt(request.getHeader(TOKEN_HEADER_NAME));
                        } catch (GlobalException e) {
                        }
                    }
                    if(tokenVo == null){
                        tokenVo = new TokenVo();
                    }
                }
            }
        }
    }

    public void init(){
        if(tokenVo == null) {
            Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
            if (details instanceof TokenVo) {
                tokenVo = (TokenVo) details;
            }
        }
        if(tokenVo == null){
            tokenVo = new TokenVo();
        }
    }

    public TokenVo getTokenVo(HttpServletRequest request) {
        if(tokenVo == null){
            if(request != null){
                init(request);
                return tokenVo == null ? new TokenVo() : tokenVo;
            }else{
                init();
                return tokenVo == null ? new TokenVo() : tokenVo;
            }
        }else{
            return tokenVo;
        }
    }

    public boolean isLogin(HttpServletRequest request) {
        return getTokenVo(request).isLogin();
    }

    public String getMemNo(HttpServletRequest request) {
        return getTokenVo(request).getMemNo();
    }

    public String getAuthToken(HttpServletRequest request){
        String authToken = getTokenVo(request).getAuthToken();
        if(DalbitUtil.isEmpty(authToken)){
            authToken = request.getHeader(TOKEN_HEADER_NAME);
        }
        return DalbitUtil.isEmpty(authToken) ? "" : authToken;
    }
}
