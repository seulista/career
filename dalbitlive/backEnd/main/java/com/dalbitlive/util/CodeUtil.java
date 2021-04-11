package com.dalbitlive.util;

import com.dalbitlive.main.vo.CodeVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CodeUtil {
    @Autowired
    private RedisUtil redisUtil;

    @Value("${redis.api.code}")
    private String REDIS_CODE_NAME;

    public List<CodeVo> getCodeList(String parentCd){
        if(DalbitUtil.isEmpty(parentCd)){
            return new ArrayList<>();
        }

        List<CodeVo> codeList = redisUtil.getHashList(REDIS_CODE_NAME, parentCd, CodeVo.class);
        if(DalbitUtil.isEmpty(codeList)){
            codeList = new ArrayList<>();
            List<CodeVo> allList = redisUtil.getHashList(REDIS_CODE_NAME, "all", CodeVo.class);
            for(CodeVo codeVo : allList){
                if(parentCd.equals(codeVo.getCd())){
                    codeList.add(codeVo);
                }
            }
        }
        return codeList;
    }

    public CodeVo getCode(String cd, String cdNm){
        CodeVo codeVo = new CodeVo();
        if(!DalbitUtil.isEmpty(cd) && !DalbitUtil.isEmpty(cdNm)){
            List<CodeVo> allList = redisUtil.getHashList(REDIS_CODE_NAME, "all", CodeVo.class);
            for(CodeVo data : allList){
                if(cd.equals(data.getCd()) && cdNm.equals(data.getCdNm())){
                    codeVo = data;
                    break;
                }
            }
        }
        return codeVo;
    }

    public CodeVo getCodeByVal(String cd, String value){
        CodeVo codeVo = new CodeVo();
        if(!DalbitUtil.isEmpty(cd) && !DalbitUtil.isEmpty(value)){
            List<CodeVo> allList = redisUtil.getHashList(REDIS_CODE_NAME, "all", CodeVo.class);
            for(CodeVo data : allList){
                if(cd.equals(data.getCd()) && value.equals(data.getValue())){
                    codeVo = data;
                    break;
                }
            }
        }
        return codeVo;
    }
}
