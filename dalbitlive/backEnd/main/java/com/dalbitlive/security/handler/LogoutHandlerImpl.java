package com.dalbitlive.security.handler;

import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.json.CustomObjectMapper;
import com.dalbitlive.common.vo.DeviceVo;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.common.vo.ProcedureVo;
import com.dalbitlive.member.service.MemberService;
import com.dalbitlive.member.vo.TokenVo;
import com.dalbitlive.member.vo.db.DBLoginVo;
import com.dalbitlive.member.vo.db.DBLogoutOutVo;
import com.dalbitlive.member.vo.db.DBLogoutVo;
import com.dalbitlive.util.DalbitUtil;
import com.dalbitlive.util.JwtUtil;
import com.dalbitlive.util.LoginUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@Slf4j
@Component("logoutHandler")
public class LogoutHandlerImpl implements LogoutHandler {

    @Autowired
    private MemberService memberService;
    @Autowired
    private LoginUtil loginUtil;

    @Value("${session.name}")
    private String SESSION_NAME;
    @Value("${spring.jwt.secret}")
    private String JWT_SECRET_KEY;
    @Value("${spring.jwt.duration}")
    private int JWT_DURATION;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        DalbitUtil.setHeader(request, response);
        DBLogoutVo dbLogoutVo = new DBLogoutVo();
        DeviceVo deviceVo = new DeviceVo(request);
        dbLogoutVo.setMem_no(loginUtil.getMemNo(request));
        dbLogoutVo.setDeviceUuid(deviceVo.getDeviceUuid());
        ProcedureVo procedureVo = memberService.callMemberLogout(dbLogoutVo);

        try{
            PrintWriter out = response.getWriter();
            CustomObjectMapper mapper = new CustomObjectMapper();
            if(procedureVo.getRet().equals(Status.방송중인DJ체크_방송중.getMessageCode())){
                out.print(mapper.writeValueAsString(new JsonOutputVo(Status.로그아웃실패_진행중인방송)));
            }else{
                HttpSession session = request.getSession(true);
                if(procedureVo.getRet().equals(Status.방송중인DJ체크_방송중아님.getMessageCode())){
                    DBLogoutOutVo dbLogoutOutVo = new Gson().fromJson(procedureVo.getExt(), DBLogoutOutVo.class);
                    if(!DalbitUtil.isEmpty(dbLogoutOutVo) && !DalbitUtil.isEmpty(dbLogoutOutVo.getRoomList())){
                        TokenVo tokenVo = (TokenVo)session.getAttribute(SESSION_NAME);
                        //TODO: 방송 나가기 소켓 발송 추가 게스트 종료 포함
                    }
                }
                session.invalidate();
                String browser = DalbitUtil.getUserAgent(request);
                DBLoginVo pLoginVo = new DBLoginVo("a", deviceVo.getOs(), deviceVo.getDeviceUuid(), deviceVo.getDeviceToken(), deviceVo.getAppVersion(), deviceVo.getAdId(), "", deviceVo.getIp(), browser);
                HashMap result = memberService.callMemberLogin(pLoginVo);
                ProcedureVo procedureLoginVo = (ProcedureVo)result.get("procedure");
                HashMap ext = new Gson().fromJson(procedureLoginVo.getExt(), HashMap.class);
                String memNo = (String)ext.get("mem_no");

                JwtUtil jwtUtil = new JwtUtil(JWT_SECRET_KEY, JWT_DURATION);
                TokenVo tokenVo = new TokenVo(jwtUtil.generateToken(memNo, false), memNo, false);
                memberService.refreshAnonymousSecuritySession("anonymousUser");
                session.setAttribute(SESSION_NAME, tokenVo);

                out.print(mapper.writeValueAsString(new JsonOutputVo(Status.로그아웃성공, tokenVo)));
            }
            out.flush();
            out.close();
        }catch(IOException e){}
    }
}
