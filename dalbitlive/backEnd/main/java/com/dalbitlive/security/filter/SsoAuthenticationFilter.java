package com.dalbitlive.security.filter;

import com.dalbitlive.common.code.ErrorStatus;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.exception.GlobalException;
import com.dalbitlive.member.vo.TokenVo;
import com.dalbitlive.security.vo.SecurityUserVo;
import com.dalbitlive.util.DalbitUtil;
import com.dalbitlive.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class SsoAuthenticationFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${sso.header.cookie.name}")
    private String SSO_HEADER_COOKIE_NAME;

    private final String[] IGNORE_URLS = {
            "/favicon.ico"
            , "/login", "/logout"
            , "/member/login", "/member/logout", "/token"
            , "/sample", "/rest/sample"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        DalbitUtil.setHeader(request, response);
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            if (!isIgnore(request)) {
                String headerAuthToken = request.getHeader(SSO_HEADER_COOKIE_NAME);
                try {
                    if(DalbitUtil.isEmptyHeaderAuthToken(headerAuthToken)){
                        if(!request.getRequestURI().startsWith("/token")){
                            throw new GlobalException(ErrorStatus.토큰검증오류, Thread.currentThread().getStackTrace()[1].getMethodName());
                        }
                    }

                    if(jwtUtil.validateToken(headerAuthToken)){
                        TokenVo tokenVo = jwtUtil.getTokenVoFromJwt(headerAuthToken);
                        if(DalbitUtil.isEmpty(tokenVo)){
                            throw new GlobalException(ErrorStatus.토큰검증오류, Thread.currentThread().getStackTrace()[1].getMethodName());
                        }
                        if (tokenVo.getMemNo() == null) {
                            throw new GlobalException(ErrorStatus.토큰검증오류, Thread.currentThread().getStackTrace()[1].getMethodName());
                        }

                        SecurityUserVo securityUserVo = null;
                        if (tokenVo.isLogin()) {
                            securityUserVo = new SecurityUserVo(tokenVo.getMemNo(), tokenVo.getMemNo(), DalbitUtil.getAuthorities());
                        }else{
                            securityUserVo = new SecurityUserVo(tokenVo.getMemNo(), tokenVo.getMemNo(), DalbitUtil.getGuestAuthorities());
                        }
                        securityUserVo.setTokenVo(tokenVo);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                securityUserVo.getTokenVo().getMemNo()
                                , securityUserVo.getTokenVo().getMemId()
                                , securityUserVo.getAuthorities());

                        SecurityContext securityContext = SecurityContextHolder.getContext();
                        securityContext.setAuthentication(authentication);
                    }else{
                        throw new GlobalException(ErrorStatus.토큰검증오류, Thread.currentThread().getStackTrace()[1].getMethodName());
                    }
                } catch (GlobalException e) {
                    PrintWriter out = response.getWriter();
                    out.print(new JsonOutputVo(e.getErrorStatus(), e.getData(), e.getMethodName()));
                    out.flush();
                    out.close();
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isIgnore(HttpServletRequest request) {
        String uri = request.getRequestURI();

        boolean result = false;
        for(String ignoreUri : IGNORE_URLS) {
            if(uri.startsWith(ignoreUri)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
