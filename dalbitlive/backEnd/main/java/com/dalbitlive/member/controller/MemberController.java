package com.dalbitlive.member.controller;

import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.exception.GlobalException;
import com.dalbitlive.member.service.MemberService;
import com.dalbitlive.member.vo.param.ParamSignUpVo;
import com.dalbitlive.util.DalbitUtil;
import com.dalbitlive.util.JwtUtil;
import com.dalbitlive.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@Scope("prototype")
public class MemberController {
    @Autowired
    private LoginUtil loginUtil;
    @Autowired
    private MemberService memberService;

    @Value("${sso.header.cookie.name}")
    private String HEADER_TOKEN_NAME;

    @GetMapping("/token")
    public JsonOutputVo token(HttpServletRequest request) throws GlobalException {
        return new JsonOutputVo(Status.조회, loginUtil.getTokenVo(request));
    }

    @PostMapping("/member/signup")
    public JsonOutputVo signup(@Valid ParamSignUpVo signUpVo, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) throws GlobalException{
        DalbitUtil.throwValidaionException(bindingResult, Thread.currentThread().getStackTrace()[1].getMethodName());

        return memberService.callMemberSignUp(signUpVo, request, response);
    }
}
