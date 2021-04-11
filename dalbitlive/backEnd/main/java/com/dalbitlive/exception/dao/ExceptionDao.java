package com.dalbitlive.exception.dao;

import com.dalbitlive.common.vo.ProcedureVo;
import org.springframework.stereotype.Repository;

@Repository
public interface ExceptionDao {
    ProcedureVo saveErrorLog(ProcedureVo procedureVo);
}
