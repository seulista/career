package com.dalbitlive.common.vo;

import com.dalbitlive.common.json.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;

/**
 * Procedure 조회를 위한 VO
 */
@Getter
@Setter
@ToString
@Scope("prototype")
public class ProcedureVo {

    public ProcedureVo(){}

    public ProcedureVo(Object paramVo){
        CustomObjectMapper mapper = new CustomObjectMapper();
        try{
            setData(mapper.writeValueAsString(paramVo));
        }catch(JsonProcessingException e){
            setData(new Gson().toJson(paramVo));
        }
    }

    public ProcedureVo(Object paramVo, boolean isHtmlEscape){
        CustomObjectMapper mapper = new CustomObjectMapper();
        try{
            setData(mapper.writeValueAsString(paramVo));
        }catch(JsonProcessingException e){
            setData(new Gson().toJson(paramVo));
        }
    }

    public ProcedureVo(String nickNm){
        setNickName(nickNm);
    }

    public ProcedureVo(String phoneNo, String password){
        setPhoneNo(phoneNo);
        setPassword(password);
    }

    private String data;
    private Object box;
    private String ret;
    private String ext;
    private String phoneNo;
    private String password;
    private String nickName;
}
