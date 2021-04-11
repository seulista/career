package com.dalbitlive.member.service;

import com.dalbitlive.common.code.Code;
import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.vo.*;
import com.dalbitlive.exception.GlobalException;
import com.dalbitlive.member.dao.MemberDao;
import com.dalbitlive.member.vo.TokenVo;
import com.dalbitlive.member.vo.db.*;
import com.dalbitlive.member.vo.param.ParamSignUpVo;
import com.dalbitlive.security.vo.SecurityUserVo;
import com.dalbitlive.util.*;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@Transactional
public class MemberService {
    @Autowired
    private MemberDao memberDao;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private InforexRestUtil restUtil;

    @Value("${session.name}")
    private String sessionName;

    public HashMap callMemberLogin(DBLoginVo dbLoginVo){
        ProcedureVo procedureVo = new ProcedureVo(dbLoginVo);
        List<?> resultSets = memberDao.callMemberLogin(procedureVo);
        HashMap result = new HashMap();
        result.put("procedure", procedureVo);
        result.put("resultSet", resultSets);
        return result;
    }

    @Transactional(readOnly = true)
    public List<?> callMemberTokenInfo(String memNo){
        HashMap data = new HashMap();
        data.put("mem_no", memNo);
        ProcedureVo procedureVo = new ProcedureVo(data);
        return memberDao.callMemberTokenInfo(procedureVo);
    }

    public ProcedureVo callMemberLogout(DBLogoutVo dbLogoutVo){
        ProcedureVo procedureVo = new ProcedureVo(dbLogoutVo);
        memberDao.callMemberLogout(procedureVo);
        return procedureVo;
    }

    /**
     * 비회원 토큰 업데이트
     */
    public void refreshAnonymousSecuritySession(String memNo){
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(memNo, "", authorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
    }

    public JsonOutputVo callMemberSignUp(ParamSignUpVo signUpVo, HttpServletRequest request, HttpServletResponse response) throws GlobalException {
        CookieUtil cookieUtil = new CookieUtil(request);
        Cookie smsCookie = cookieUtil.getCookie("smsCookie");
        String s_phoneNo = "";
        if (DalbitUtil.isEmpty(smsCookie) && "p".equals(signUpVo.getMemType())) {
            return new JsonOutputVo(Status.인증번호요청_유효하지않은번호);
        }

        if(DalbitUtil.isCheckSlash(signUpVo.getMemId())){
            return new JsonOutputVo(Status.부적절한문자열);
        }

        if(DalbitUtil.isEmpty(signUpVo.getProfImg()) || signUpVo.getProfImg().startsWith(Code.포토_프로필_디폴트_PREFIX.getCode())){
            signUpVo.setProfImg("");
        }

        DeviceVo deviceVo = new DeviceVo(request);
        int os = deviceVo.getOs();
        String deviceId = deviceVo.getDeviceUuid();
        String deviceToken = deviceVo.getDeviceToken();
        String appVer = deviceVo.getAppVersion();
        String appAdId = deviceVo.getAdId();
        String ip = deviceVo.getIp();
        String browser = DalbitUtil.getUserAgent(request);
        String nativeTid = DalbitUtil.isEmpty(signUpVo.getNativeTid()) ? "" : signUpVo.getNativeTid();

        DBSignUpVo dbSignUpVo = new DBSignUpVo(
                signUpVo
                , os
                , deviceId
                , deviceToken
                , appVer
                , appAdId
                , ""
                , ip
                , browser
                , nativeTid
        );

        if("p".equals(signUpVo.getMemType())) {
            HashMap cookieResMap = new Gson().fromJson(URLDecoder.decode(smsCookie.getValue()), HashMap.class);
            s_phoneNo = (String) cookieResMap.get("phoneNo");
            s_phoneNo = DalbitUtil.isEmpty(s_phoneNo) ? "" : s_phoneNo.replaceAll("-", "");
        }

        if(!"p".equals(dbSignUpVo.getMemSlct()) || ("p".equals(dbSignUpVo.getMemSlct()) && s_phoneNo.equals(dbSignUpVo.getId()))){
            boolean isDone = false;
            if(dbSignUpVo.getProfileImage().startsWith(Code.포토_프로필_임시_PREFIX.getCode())){
                isDone = true;
            }
            dbSignUpVo.setProfileImage(DalbitUtil.replacePath(dbSignUpVo.getProfileImage()));
            ProcedureVo procedureVo = new ProcedureVo(dbSignUpVo);
            List<?> resultSets = memberDao.callMemberSignUp(procedureVo);
            if(Status.회원가입성공.getMessageCode().equals(procedureVo.getRet())){
                if(isDone) {
                    restUtil.imgDone(DalbitUtil.replaceDonePath(dbSignUpVo.getProfileImage()), request);
                }
                DBLoginInfoOutVo dbLoginInfoOutVo = DBUtil.getData(resultSets, 0, DBLoginInfoOutVo.class);
                List<DBProfImgOutVo> profImgList = DBUtil.getList(resultSets, 1, DBProfImgOutVo.class);

                TokenVo tokenVo = new TokenVo(dbLoginInfoOutVo, profImgList);
                tokenVo.setAuthToken(jwtUtil.generateToken(dbLoginInfoOutVo.getMemNo(), true));

                HttpSession session = request.getSession(true);
                session.setAttribute(sessionName, tokenVo);

                SecurityUserVo securityUserVo = new SecurityUserVo(tokenVo.getMemNo(), tokenVo.getMemNo(), DalbitUtil.getAuthorities());
                securityUserVo.setTokenVo(tokenVo);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        securityUserVo.getTokenVo().getMemNo()
                        , securityUserVo.getTokenVo().getMemId()
                        , securityUserVo.getAuthorities());
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authentication);

                AdbrixVo adbrixVo = new AdbrixVo();
                adbrixVo.setAge(String.valueOf(tokenVo.getAge()));
                adbrixVo.setSex(tokenVo.getGender());
                //adbrixVo.setConnecttime();
                AdbrixLayoutVo adbrixLayoutVo = new AdbrixLayoutVo();
                adbrixLayoutVo.setEventName("signUp");
                adbrixLayoutVo.setAttr(adbrixVo);

                HashMap resultMap = new HashMap();
                resultMap.put("tokenInfo", tokenVo);
                resultMap.put("adbrixData", adbrixLayoutVo);

                try {
                    response.addCookie(CookieUtil.deleteCookie("smsCookie", "", "/", 0));
                } catch (IOException e) {}

                return new JsonOutputVo(Status.회원가입성공, resultMap);
            }else if (Status.회원가입실패_중복가입.getMessageCode().equals(procedureVo.getRet())){
                return new JsonOutputVo(Status.회원가입실패_중복가입);
            }else if (Status.회원가입실패_닉네임중복.getMessageCode().equals(procedureVo.getRet())){
                return new JsonOutputVo(Status.회원가입실패_닉네임중복);
            }else if (Status.회원가입실패_파라메터오류.getMessageCode().equals(procedureVo.getRet())){
                return new JsonOutputVo(Status.파라미터오류);
            }else if (Status.회원가입실패_탈퇴회원.getMessageCode().equals(procedureVo.getRet())){
                return new JsonOutputVo(Status.회원가입실패_탈퇴회원);
            }else{
                return new JsonOutputVo(Status.회원가입오류);
            }
        }else{
            return new JsonOutputVo(Status.인증번호요청_유효하지않은번호);
        }
    }
}
