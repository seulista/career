package com.dalbitlive.exception.vo.db;

import com.dalbitlive.common.vo.DBCommonVo;
import com.dalbitlive.common.vo.DeviceVo;
import com.dalbitlive.exception.vo.param.ParamErrorVo;
import com.dalbitlive.util.DalbitUtil;
import com.dalbitlive.util.LoginUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Getter @Setter @Component
public class DBErrorVo extends DBCommonVo {
    @Autowired
    private LoginUtil loginUtil;
    private String mem_no;
    private String os;
    private String version;
    private String build;
    private String dtype;
    private String ctype;
    private String desc;

    public DBErrorVo(){}

    public DBErrorVo(ParamErrorVo paramErrorVo, HttpServletRequest request) {
        DeviceVo deviceVo = new DeviceVo(request);
        setMem_no(loginUtil.getMemNo(request));
        if(DalbitUtil.isEmpty(request.getParameter("os"))){
            setOs(String.valueOf(deviceVo.getOs()));
        } else {
            setOs(request.getParameter("os"));
        }
        setVersion(deviceVo.getAppVersion());
        setBuild(deviceVo.getAppBuild());
        setDtype(paramErrorVo.getDataType());
        setCtype(paramErrorVo.getCommandType());
        setDesc(paramErrorVo.getDesc());
    }
}
