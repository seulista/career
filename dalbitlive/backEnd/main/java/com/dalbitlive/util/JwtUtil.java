package com.dalbitlive.util;

import com.dalbitlive.common.code.ErrorStatus;
import com.dalbitlive.common.vo.DeviceVo;
import com.dalbitlive.common.vo.ProcedureVo;
import com.dalbitlive.exception.GlobalException;
import com.dalbitlive.member.service.MemberService;
import com.dalbitlive.member.vo.TokenVo;
import com.dalbitlive.member.vo.db.DBLoginInfoOutVo;
import com.dalbitlive.member.vo.db.DBLoginVo;
import com.dalbitlive.member.vo.db.DBProfImgOutVo;
import com.google.gson.Gson;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    @Autowired
    HttpServletRequest request;
    @Autowired
    MemberService memberService;

    @Value("${spring.jwt.secret}")
    private String JWT_SECRET_KEY;
    @Value("${spring.jwt.duration}")
    private int JWT_DURATION;
    @Value("${session.name}")
    private String sessionName;

    final String JWT_SEPARATOR = "@";

    JwtUtil(){}
    public JwtUtil(String JWT_SECRET_KEY, int JWT_DURATION){
        this.JWT_SECRET_KEY = JWT_SECRET_KEY;
        this.JWT_DURATION = JWT_DURATION;
    }

    /**
     * 이름으로 Jwt Token을 생성한다.
     */
    public String generateToken(String name, long expireTime) {
        return Jwts.builder()
                .setId(name)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰 발행일자
                .setExpiration(new Date(System.currentTimeMillis() + expireTime)) // 유효시간 설정 (30일 기준)
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY) // 암호화 알고리즘, secret값 세팅
                .compact();
    }

    public String generateToken(String name) {
        return generateToken(name, 2592000000L);
    }
    public String generateToken(String memNo, boolean isLogin) {
        return generateToken(memNo, isLogin, 2592000000L);
    }
    public String generateToken(String memNo, boolean isLogin, long expireTime) {
        return generateToken(memNo + JWT_SEPARATOR + isLogin, expireTime);

    }
    /**
     * Jwt Token을 복호화 하여 이름을 얻는다.
     */
    public String getUserNameFromJwt(String jwt) throws GlobalException {
        return getClaims(jwt).getBody().getId();
    }

    public TokenVo getTokenVoFromJwt(String jwt) throws GlobalException{
        try {
            if(DalbitUtil.isEmpty(jwt)){
                HttpSession session = request.getSession(true);
                TokenVo tokenVo = (TokenVo)session.getAttribute(sessionName);
                if(DalbitUtil.isEmpty(tokenVo) || DalbitUtil.isEmpty(tokenVo.getAuthToken())){
                    String browser = DalbitUtil.getUserAgent(request);
                    DeviceVo deviceVo = new DeviceVo(request);
                    DBLoginVo pLoginVo = new DBLoginVo("a", deviceVo.getOs(), deviceVo.getDeviceUuid(), deviceVo.getDeviceToken(), deviceVo.getAppVersion(), deviceVo.getAdId(), "", deviceVo.getIp(), browser);
                    HashMap result = memberService.callMemberLogin(pLoginVo);
                    ProcedureVo procedureVo = (ProcedureVo)result.get("procedure");
                    HashMap ext = new Gson().fromJson(procedureVo.getExt(), HashMap.class);
                    String memNo = (String)ext.get("mem_no");
                    tokenVo = new TokenVo(generateToken(memNo, false), memNo, false);
                    memberService.refreshAnonymousSecuritySession(memNo);
                    session.setAttribute(sessionName, tokenVo);
                    return tokenVo;
                }
                jwt = tokenVo.getAuthToken();
            }
            String[] splitStrArr = getUserNameFromJwt(jwt).split(JWT_SEPARATOR);
            if (splitStrArr.length == 2 || splitStrArr.length == 3) {

                if(DalbitUtil.isEmpty(splitStrArr[0]) || DalbitUtil.isEmpty(splitStrArr[1])){
                    throw new GlobalException(ErrorStatus.토큰검증오류, "회원번호 or 로그인 여부가 null값 입니다.");
                }

                boolean isLogin = Boolean.valueOf(splitStrArr[1]);
                // 프로퍼티에(JWT_DURATION)시간 이내(현재 24 시간) 토큰일경우 재발행 하지 않음
                boolean isNew = true;
                Jws<Claims> jwtClaims = getClaims(jwt);
                Object issuedAt = jwtClaims.getBody().get(Claims.ISSUED_AT);
                if(issuedAt != null){
                    long issuedTs = Long.valueOf(issuedAt.toString());
                    if((new Date().getTime() / 1000 - issuedTs) < (3600 * JWT_DURATION)){
                        isNew = false;
                    }
                }
                HttpSession session = request.getSession(true);
                TokenVo tokenVo = (TokenVo)session.getAttribute(sessionName);
                if(tokenVo != null && (isLogin != tokenVo.isLogin() || !splitStrArr[0].equals(tokenVo.getMemNo()))){
                    tokenVo = null;
                }
                if(tokenVo == null){
                    if(isLogin){
                        List<?> resultSet = memberService.callMemberTokenInfo(splitStrArr[0]);
                        if(!DalbitUtil.isEmpty(resultSet) && resultSet.size() == 2){
                            DBLoginInfoOutVo dbLoginInfoOutVo = DBUtil.getData(resultSet, 0, DBLoginInfoOutVo.class);
                            List<DBProfImgOutVo> profImgList = DBUtil.getList(resultSet, 1, DBProfImgOutVo.class);
                            tokenVo = new TokenVo(dbLoginInfoOutVo, profImgList);
                            tokenVo.setAuthToken(jwt);
                        }else{ //비회원 로그인
                            String browser = DalbitUtil.getUserAgent(request);
                            DeviceVo deviceVo = new DeviceVo(request) ;
                            DBLoginVo pLoginVo = new DBLoginVo("a", deviceVo.getOs(), deviceVo.getDeviceUuid(), deviceVo.getDeviceToken(), deviceVo.getAppVersion(), deviceVo.getAdId(), "", deviceVo.getIp(), browser);
                            HashMap result = memberService.callMemberLogin(pLoginVo);
                            ProcedureVo procedureVo = (ProcedureVo)result.get("procedure");
                            HashMap ext = new Gson().fromJson(procedureVo.getExt(), HashMap.class);
                            String memNo = (String)ext.get("mem_no");

                            tokenVo = new TokenVo(generateToken(memNo, false), memNo, false);
                            memberService.refreshAnonymousSecuritySession(memNo);
                            isNew = false;
                            isLogin = false;
                        }
                    }else{
                        tokenVo = new TokenVo(jwt, splitStrArr[0], isLogin);
                    }
                }else{
                    if(isLogin && isNew){
                        List<?> resultSet = memberService.callMemberTokenInfo(splitStrArr[0]);
                        if(!DalbitUtil.isEmpty(resultSet) && resultSet.size() == 2){
                            DBLoginInfoOutVo dbLoginInfoOutVo = DBUtil.getData(resultSet, 0, DBLoginInfoOutVo.class);
                            List<DBProfImgOutVo> profImgList = DBUtil.getList(resultSet, 1, DBProfImgOutVo.class);
                            tokenVo = new TokenVo(dbLoginInfoOutVo, profImgList);
                            tokenVo.setAuthToken(jwt);
                        }else{
                            String browser = DalbitUtil.getUserAgent(request);
                            DeviceVo deviceVo = new DeviceVo(request) ;
                            DBLoginVo pLoginVo = new DBLoginVo("a", deviceVo.getOs(), deviceVo.getDeviceUuid(), deviceVo.getDeviceToken(), deviceVo.getAppVersion(), deviceVo.getAdId(), "", deviceVo.getIp(), browser);
                            HashMap result = memberService.callMemberLogin(pLoginVo);
                            ProcedureVo procedureVo = (ProcedureVo)result.get("procedure");
                            HashMap ext = new Gson().fromJson(procedureVo.getExt(), HashMap.class);
                            String memNo = (String)ext.get("mem_no");

                            tokenVo = new TokenVo(generateToken(memNo, false), memNo, false);
                            memberService.refreshAnonymousSecuritySession(memNo);
                            isNew = false;
                            isLogin = false;
                        }
                    }
                }
                if(isNew){
                    tokenVo.setAuthToken(generateToken(splitStrArr[0], isLogin));
                }

                session.setAttribute(sessionName, tokenVo);
                return tokenVo;
            }else{
                throw new GlobalException(ErrorStatus.토큰검증오류, "회원번호 or 로그인 여부가 없습니다.");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new GlobalException(ErrorStatus.토큰검증오류, "이상한 토큰이 넘어왔어요.");
        }
    }

    /**
     * Jwt Token의 유효성을 체크한다.
     */
    public boolean validateToken(String jwt) throws GlobalException {
        return this.getClaims(jwt) != null;
    }

    private Jws<Claims> getClaims(String jwt) throws GlobalException {
        try {
            return Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(jwt);
        } catch (SignatureException ex) {
            log.debug("Invalid JWT signature");
            throwGlobalException(ErrorStatus.토큰검증오류);

        } catch (MalformedJwtException ex) {
            log.debug("Invalid JWT token");
            throwGlobalException(ErrorStatus.토큰검증오류);

        } catch (ExpiredJwtException ex) {
            log.debug("Expired JWT token");
            throwGlobalException(ErrorStatus.토큰만료오류);

        } catch (UnsupportedJwtException ex) {
            log.debug("Unsupported JWT token");
            throwGlobalException(ErrorStatus.토큰검증오류);

        } catch (IllegalArgumentException ex) {
            log.debug("JWT claims string is empty.");
            throwGlobalException(ErrorStatus.토큰검증오류);
        }

        return null;
    }

    private void throwGlobalException(ErrorStatus errorStatus) throws GlobalException{
        if(!DalbitUtil.isEmpty(request) && request.getRequestURI().startsWith("/token")){
            String browser = DalbitUtil.getUserAgent(request);
            DeviceVo deviceVo = new DeviceVo(request) ;
            DBLoginVo pLoginVo = new DBLoginVo("a", deviceVo.getOs(), deviceVo.getDeviceUuid(), deviceVo.getDeviceToken(), deviceVo.getAppVersion(), deviceVo.getAdId(), "", deviceVo.getIp(), browser);
            HashMap result = memberService.callMemberLogin(pLoginVo);
            ProcedureVo procedureVo = (ProcedureVo)result.get("procedure");
            HashMap ext = new Gson().fromJson(procedureVo.getExt(), HashMap.class);
            String memNo = (String)ext.get("mem_no");

            TokenVo tokenVo = new TokenVo(generateToken(memNo, false), memNo, false);
            memberService.refreshAnonymousSecuritySession(memNo);
            HttpSession session = request.getSession(true);
            session.setAttribute(sessionName, tokenVo);
            throw new GlobalException(errorStatus, tokenVo, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        throw new GlobalException(errorStatus, Thread.currentThread().getStackTrace()[1].getMethodName());
    }
}
