package com.dalbitlive.security.service;

import com.dalbitlive.common.annotation.NoLogging;
import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.vo.DeviceVo;
import com.dalbitlive.common.vo.ProcedureVo;
import com.dalbitlive.exception.CustomUsernameNotFoundException;
import com.dalbitlive.member.service.MemberService;
import com.dalbitlive.member.vo.TokenVo;
import com.dalbitlive.member.vo.db.DBLoginInfoOutVo;
import com.dalbitlive.member.vo.db.DBLoginVo;
import com.dalbitlive.member.vo.db.DBProfImgOutVo;
import com.dalbitlive.security.vo.SecurityUserVo;
import com.dalbitlive.util.DBUtil;
import com.dalbitlive.util.DalbitUtil;
import com.dalbitlive.util.JwtUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${session.name}")
    private String SESSION_NAME;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HashMap map = DalbitUtil.getParameterMap(request);

        if(DalbitUtil.isEmpty(map.get("memId"))) {
            throw new CustomUsernameNotFoundException(Status.???????????????_??????????????????);
        }

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        SecurityUserVo securityUserVo = new SecurityUserVo(DalbitUtil.getStringMap(map, "memId"), DalbitUtil.getStringMap(map, "memId"), authorities);
        return securityUserVo;
    }

    public UserDetails loadUserByUsername() throws CustomUsernameNotFoundException{
        HashMap map = DalbitUtil.getParameterMap(request);
        if("p".equals(DalbitUtil.getStringMap(map, "memType")) && DalbitUtil.isEmpty(DalbitUtil.getStringMap(map, "memPwd"))){
            throw new CustomUsernameNotFoundException(Status.??????????????????);
        }
        DeviceVo deviceVo = new DeviceVo(request);
        DBLoginVo dbLoginVo = new DBLoginVo(
                DalbitUtil.getStringMap(map, "memType")
                , DalbitUtil.getStringMap(map, "memId")
                , DalbitUtil.getStringMap(map, "memPwd")
                , deviceVo.getOs()
                , deviceVo.getDeviceUuid()
                , deviceVo.getDeviceToken()
                , deviceVo.getAppVersion()
                , deviceVo.getAdId()
                , ""
                , deviceVo.getIp()
                , DalbitUtil.getUserAgent(request)
                , deviceVo.getAppBuild()
        );
        dbLoginVo.setRoom_no(request.getParameter("room_no") == null ? "" : request.getParameter("room_no"));
        HashMap loginResult = memberService.callMemberLogin(dbLoginVo);
        if(loginResult == null || !loginResult.containsKey("procedure")){
            throw new CustomUsernameNotFoundException(Status.???????????????);
        }
        ProcedureVo procedureVo = (ProcedureVo)loginResult.get("procedure");

        if(procedureVo.getRet().equals(Status.???????????????_??????????????????.getMessageCode())) {
            if("p".equals(DalbitUtil.getStringMap(map, "memType"))){
                throw new CustomUsernameNotFoundException(Status.???????????????_??????????????????);
            }else{
                throw new CustomUsernameNotFoundException(Status.???????????????_??????????????????);
            }
        }else if(procedureVo.getRet().equals(Status.???????????????_??????????????????.getMessageCode())) {
            throw new CustomUsernameNotFoundException(Status.???????????????_??????????????????);
        }else if(procedureVo.getRet().equals(Status.???????????????_??????????????????.getMessageCode())) {
            throw new CustomUsernameNotFoundException(Status.???????????????_??????????????????);
        }else if(procedureVo.getRet().equals(Status.???????????????_????????????.getMessageCode()) || procedureVo.getRet().equals(Status.???????????????_????????????.getMessageCode())) {
            Status resultStatus = Status.???????????????_????????????;
            HashMap resultMap = new Gson().fromJson(procedureVo.getExt(), HashMap.class);
            HashMap returnMap = new HashMap();
            if(procedureVo.getRet().equals(Status.???????????????_????????????.getMessageCode())){
                resultStatus = Status.???????????????_????????????;
                returnMap.put("blockDay", DalbitUtil.getStringMap(resultMap, "block_day"));
                returnMap.put("blockEndDt", DalbitUtil.getUTCFormat(DalbitUtil.getDateMap(resultMap, "expected_end_date")));
                returnMap.put("blockEndTs", DalbitUtil.getUTCTimeStamp(DalbitUtil.getDateMap(resultMap, "expected_end_date")));
            }
            returnMap.put("opCode", DalbitUtil.getStringMap(resultMap, "opCode"));
            returnMap.put("opMsg", DalbitUtil.getStringMap(resultMap, "opMsg"));

            throw new CustomUsernameNotFoundException(resultStatus, returnMap);
        }else if(procedureVo.getRet().equals(Status.???????????????_??????.getMessageCode())) {
            throw new CustomUsernameNotFoundException(Status.???????????????_??????);
        }else if(procedureVo.getRet().equals(Status.???????????????_???????????????.getMessageCode())){
            HashMap resultMap = new Gson().fromJson(procedureVo.getExt(), HashMap.class);
            HashMap returnMap = new HashMap();
            returnMap.put("memNo", DalbitUtil.getStringMap(resultMap, "mem_no"));

            throw new CustomUsernameNotFoundException(Status.???????????????_???????????????, returnMap);
        }else if(procedureVo.getRet().equals(Status.???????????????.getMessageCode()) || procedureVo.getRet().equals(Status.???????????????_????????????.getMessageCode())){
            if(!loginResult.containsKey("resultSet")){
                throw new CustomUsernameNotFoundException(Status.???????????????);
            }
            List<?> resultSet = (List<?>)loginResult.get("resultSet");
            DBLoginInfoOutVo loginInfoOutVo = DBUtil.getData(resultSet, 1, DBLoginInfoOutVo.class);
            if(loginInfoOutVo == null){
                throw new CustomUsernameNotFoundException(Status.???????????????);
            }
            List<DBProfImgOutVo> profImgList = DBUtil.getList(resultSet, 2, DBProfImgOutVo.class);
            TokenVo tokenVo = new TokenVo(loginInfoOutVo, profImgList);
            if(procedureVo.getRet().equals(Status.???????????????_????????????.getMessageCode())){
                tokenVo.setLogin(false);
            }else{
                tokenVo.setLogin(true);
            }
            tokenVo.setAuthToken(jwtUtil.generateToken(tokenVo.getMemNo(), tokenVo.isLogin()));
            SecurityUserVo securityUserVo = new SecurityUserVo(
                    tokenVo.getMemNo()
                    , tokenVo.getMemId()
                    , procedureVo.getRet().equals(Status.???????????????.getMessageCode()) ? (tokenVo.isAdmin() ? DalbitUtil.getAdminAuthorities() : DalbitUtil.getAuthorities()) : DalbitUtil.getSleepAuthorities());
            securityUserVo.setTokenVo(tokenVo);
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_NAME, tokenVo);
            return securityUserVo;
        }else{
            throw new CustomUsernameNotFoundException(Status.???????????????);
        }
    }
}
