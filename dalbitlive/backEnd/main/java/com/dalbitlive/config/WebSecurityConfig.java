package com.dalbitlive.config;

import com.dalbitlive.security.filter.SsoAuthenticationFilter;
import com.dalbitlive.security.handler.LogoutHandlerImpl;
import com.dalbitlive.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@ComponentScan({"com.dalbitlive"})
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired private AuthenticationSuccessHandler authSuccessHandler;
    @Autowired private AuthenticationFailureHandler authFailureHandler;
    @Autowired private LogoutHandlerImpl logoutHandler;
    @Autowired private UserDetailsServiceImpl userDetailsService;
    @Autowired private AuthenticationProvider authProvider;
    @Autowired private SsoAuthenticationFilter ssoAuthenticationFilter;

    @Bean
    public DelegatingPasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder delegatingPasswordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegatingPasswordEncoder;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
            .ignoring()
            .antMatchers(
                    "/**.html"
                    , "/favicon.ico"
                    , "/robots.txt"
            );
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .csrf().disable() // ???????????? on??? csrf ????????? ????????? ????????????. on?????? ???????????? ?????? ??????????????? ?????????????????? ??????????????? ?????????.
            .formLogin() // ???????????? ????????? ???????????? ????????? ???????????? ????????????.
            .loginPage("/login")
            .loginProcessingUrl("/member/login")
            .defaultSuccessUrl("/login/success")

            .usernameParameter("s_id")         //id
            .passwordParameter("s_pwd")      //password

            .successHandler(authSuccessHandler) //????????? ?????? ??? ??????
            .failureHandler(authFailureHandler) //????????? ?????? ??? ??????

            .and()
            .addFilterBefore(ssoAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)   //cookie ?????? sso ?????? ??????
            .exceptionHandling().accessDeniedPage("/login")

            .and()
            .userDetailsService(userDetailsService)
            .authorizeRequests()
            .antMatchers(
                    "/error**"
            ).permitAll()       //?????? ?????? ??????

            .antMatchers(
                    "/sample"
            ).hasRole("USER") // USER ????????? ?????? ??????
            .anyRequest().permitAll() // ????????? ???????????? ?????? ?????? ??????

            .and()
            .logout()
            .logoutUrl("/member/logout")
            .addLogoutHandler(logoutHandler)
            .invalidateHttpSession(false)
            .and()
            .sessionManagement()
      ;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
