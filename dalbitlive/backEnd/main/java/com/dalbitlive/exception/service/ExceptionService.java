package com.dalbitlive.exception.service;

import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.vo.DeviceVo;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.common.vo.ProcedureVo;
import com.dalbitlive.exception.dao.ExceptionDao;
import com.dalbitlive.exception.vo.db.DBErrorVo;
import com.dalbitlive.exception.vo.param.ParamErrorVo;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
@Transactional
public class ExceptionService {
    @Autowired
    ExceptionDao exceptionDao;

    @Value("${sso.header.cookie.name}")
    private String SSO_HEADER_COOKIE_NAME;

    public JsonOutputVo saveErrorLog(ParamErrorVo pErrorLogVo){
        return saveErrorLog(pErrorLogVo, null);
    }
    public JsonOutputVo saveErrorLog(ParamErrorVo pErrorLogVo, HttpServletRequest request){
        DBErrorVo dbErrorLogVo = new DBErrorVo(pErrorLogVo, request);
        if(request != null && request instanceof HttpServletRequest){
            DeviceVo deviceVo  = new DeviceVo(request);
            String desc = pErrorLogVo.getDesc();
            desc = "AuthToken : " + request.getHeader(SSO_HEADER_COOKIE_NAME) + "\n" +desc;
            desc = deviceVo.toString() + "\n" +desc;
            dbErrorLogVo.setDesc(desc);
        }

        return saveErrorLog(dbErrorLogVo);
    }

    public JsonOutputVo saveErrorLog(DBErrorVo dbErrorLogVo, HttpServletRequest request){
        if(request != null && request instanceof HttpServletRequest){
            DeviceVo deviceVo  = new DeviceVo(request);
            String desc = dbErrorLogVo.getDesc();
            desc = "AuthToken : " + request.getHeader(SSO_HEADER_COOKIE_NAME) + "\n" +desc;
            desc = deviceVo.toString() + "\n" +desc;
            dbErrorLogVo.setDesc(desc);
        }

        return saveErrorLog(dbErrorLogVo);
    }

    public JsonOutputVo saveErrorLog(DBErrorVo dbErrorLogVo){
        ProcedureVo procedureVo = new ProcedureVo(dbErrorLogVo);
        exceptionDao.saveErrorLog(procedureVo);

        if(procedureVo.getRet().equals(Status.에러로그저장_성공.getMessageCode())) {
            return new JsonOutputVo(Status.에러로그저장_성공);
        } else {
            return new JsonOutputVo(Status.에러로그저장_실패);
        }
    }
}
