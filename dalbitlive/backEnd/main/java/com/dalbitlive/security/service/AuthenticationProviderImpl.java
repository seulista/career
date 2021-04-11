package com.dalbitlive.security.service;

import com.dalbitlive.security.vo.SecurityUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component("authProvider")
public class AuthenticationProviderImpl implements AuthenticationProvider {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SecurityUserVo securityUserVo = (SecurityUserVo)userDetailsService.loadUserByUsername();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                securityUserVo.getTokenVo().getMemNo()
                , securityUserVo.getTokenVo().getMemId()
                , securityUserVo.getAuthorities());
        token.setDetails(securityUserVo.getTokenVo());
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
