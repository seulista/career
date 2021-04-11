package com.dalbitlive.exception.conrtoller;

//import com.dalbitlive.broadcast.vo.RoomInfoVo;
import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.vo.DeviceVo;
import com.dalbitlive.common.vo.JsonOutputVo;
import com.dalbitlive.exception.GlobalException;
import com.dalbitlive.exception.service.ExceptionService;
import com.dalbitlive.exception.vo.db.DBErrorVo;
import com.dalbitlive.util.DalbitUtil;
import com.dalbitlive.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@Slf4j
@ControllerAdvice
@RestController
@Scope("prototype")
public class CommonErrorController {
    @Autowired
    ExceptionService exceptionService;
    @Autowired
    private LoginUtil loginUtil;

    /**
     * 에러로그 예외처리
     * false : 로그 적재 안함
     * true : 로그 적재
     */
    public boolean isSaveLog(GlobalException globalException, HttpServletResponse response, HttpServletRequest request){
        if("local".equals(DalbitUtil.getActiveProfile())) return false;
        if(globalException.getStatus() == Status.벨리데이션체크) return false;
        if(request.getRequestURL().toString().endsWith("/error/log")) return false;
        if(request.getRequestURL().toString().endsWith("/ctrl/check/service")) return false;

        //Broken pipe 처리
        if(globalException.getClass().getSimpleName().toLowerCase().equals("clientabortexception")) return false; //ClientAbortException
        if(globalException.getClass().getSimpleName().toLowerCase().contains("clientabortexception")) return false; //ClientAbortException
        if(globalException.getMessage().contains("Broken pipe")) return false;

        return true;
    }

    @ExceptionHandler(GlobalException.class)
    public JsonOutputVo exceptionHandle(GlobalException globalException, HttpServletResponse response, HttpServletRequest request){
        DalbitUtil.setHeader(request, response);

        if("local".equals(DalbitUtil.getActiveProfile())){
            globalException.printStackTrace();
        }

        try {
            if(isSaveLog(globalException, response, request)) {
                DeviceVo deviceVo = new DeviceVo(request);
                DBErrorVo apiData = new DBErrorVo();
                apiData.setOs("API");
                apiData.setDtype(deviceVo.getOs() + "|" + globalException.getMethodName());
                apiData.setVersion(deviceVo.getAppVersion());
                apiData.setBuild(deviceVo.getAppBuild());
                apiData.setMem_no(loginUtil.getMemNo(request));
                apiData.setCtype(request.getRequestURL().toString());
                String desc = "";
                if (!DalbitUtil.isEmpty(globalException.getData())) {
                    desc = "Data : \n" + globalException.getData().toString() + "\n";
                }
                if (!DalbitUtil.isEmpty(globalException.getValidationMessageDetail())) {
                    desc += "Validation : \n" + globalException.getValidationMessageDetail().toString() + "\n";
                }
                desc += "DeviceVo : \n" + deviceVo.toString();
                desc += "AuthToken : \n" + request.getHeader(DalbitUtil.getProperty("sso.header.cookie.name"));
                StringWriter sw = new StringWriter();
                globalException.printStackTrace(new PrintWriter(sw));
                if (sw != null) {
                    desc += "GlobalException : \n" + sw.toString();
                }
                apiData.setDesc(desc);
                if(!desc.toLowerCase().contains("clientabortexception") && !desc.toLowerCase().contains("broken pipe")){
                    exceptionService.saveErrorLog(apiData, request);
                }
            }
        }catch (Exception e){}

        if(globalException.getErrorStatus() != null){
            if(request.getRequestURL().toString().endsWith("/broad/vw/create") && globalException.getData() == null){
                //globalException.setData(new RoomInfoVo());
            }
            return new JsonOutputVo(globalException.getErrorStatus(), globalException.getData(), globalException.getValidationMessageDetail(), globalException.getMethodName());
        }else{
            if(globalException.isCustomMessage()){
                JsonOutputVo jsonOutputVo = new JsonOutputVo();
                jsonOutputVo.setStatus(Status.벨리데이션체크);
                String msg = "";

                try{
                    msg = ((String) globalException.getValidationMessageDetail().get(0)).split(",")[0] + "," + ((String) globalException.getValidationMessageDetail().get(0)).split(",")[2];
                }catch(ArrayIndexOutOfBoundsException ae){
                    msg = (String)globalException.getValidationMessageDetail().get(0);
                }

                jsonOutputVo.setMessage(msg);
                jsonOutputVo.setMethodName(globalException.getMethodName());
                jsonOutputVo.setTimestamp(DalbitUtil.setTimestampInJsonOutputVo());
                if(request.getRequestURL().toString().endsWith("/broad/vw/create")){
                    //jsonOutputVo.setData(new RoomInfoVo());
                }else{
                    jsonOutputVo.setData("");
                }
                return jsonOutputVo;
            }else{
                if(request.getRequestURL().toString().endsWith("/broad/vw/create") && globalException.getData() == null){
                    //globalException.setData(new RoomInfoVo());
                }
                return new JsonOutputVo(globalException.getStatus(), globalException.getData(), globalException.getValidationMessageDetail(), globalException.getMethodName());
            }

        }
    }

    @ExceptionHandler(Exception.class)
    public JsonOutputVo exceptionHandle(Exception exception, HttpServletResponse response, HttpServletRequest request){
        DalbitUtil.setHeader(request, response);

        if("local".equals(DalbitUtil.getActiveProfile())){
            exception.printStackTrace();
        }

        try {
            DeviceVo deviceVo = new DeviceVo(request);
            DBErrorVo apiData = new DBErrorVo();
            apiData.setOs("API");
            apiData.setDtype(deviceVo.getOs()+"|"+request.getMethod());
            apiData.setVersion(deviceVo.getAppVersion());
            apiData.setBuild(deviceVo.getAppBuild());
            apiData.setMem_no(loginUtil.getMemNo(request));
            apiData.setCtype(request.getRequestURL().toString());
            String desc = "";
            if(!DalbitUtil.isEmpty(request.getParameterMap())){
                desc = "Data : \n" + request.getParameterMap().toString() + "\n";
            }
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            if(sw != null){
                desc += "Exception : \n" + sw.toString();
            }
            apiData.setDesc(desc);
            exceptionService.saveErrorLog(apiData, request);
        }catch (Exception e){}

        HashMap map = new HashMap();
        map.put("message", exception.getMessage());
        return new JsonOutputVo(Status.비즈니스로직오류, map, null, request.getMethod() + "");
    }

    @ExceptionHandler(ClientAbortException.class)
    public JsonOutputVo exceptionHandle(ClientAbortException exception, HttpServletResponse response, HttpServletRequest request){
        DalbitUtil.setHeader(request, response);
        return new JsonOutputVo(Status.사용자요청취소);
    }

}
