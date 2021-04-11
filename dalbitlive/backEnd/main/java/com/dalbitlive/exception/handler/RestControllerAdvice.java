package com.dalbitlive.exception.handler;

import com.dalbitlive.common.code.ErrorStatus;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.exception.CustomUsernameNotFoundException;
import com.dalbitlive.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@org.springframework.web.bind.annotation.RestControllerAdvice
public class RestControllerAdvice {

    @Autowired
    MessageUtil messageUtil;

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public JsonOutputVo exceptionAdvice(HttpServletRequest httpServletRequest){
        return new JsonOutputVo(ErrorStatus.권한없음);
    }

    @ExceptionHandler(CustomUsernameNotFoundException.class)
    public JsonOutputVo customUsernameNotFoundExceptionAdvice(HttpServletRequest httpServletRequest, CustomUsernameNotFoundException exception){
        return new JsonOutputVo(ErrorStatus.권한없음);
    }
}
