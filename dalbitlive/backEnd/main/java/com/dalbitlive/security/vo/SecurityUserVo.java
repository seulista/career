package com.dalbitlive.security.vo;

import com.dalbitlive.member.vo.TokenVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter @Setter
public class SecurityUserVo extends User {
    private static final long serialVersionUID = 1L;

    private TokenVo tokenVo;

    public SecurityUserVo(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
}
