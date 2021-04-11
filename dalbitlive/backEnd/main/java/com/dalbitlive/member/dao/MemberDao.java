package com.dalbitlive.member.dao;

import com.dalbitlive.common.vo.ProcedureVo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberDao {
    List<?> callMemberLogin(ProcedureVo procedureVo);

    List<?> callMemberTokenInfo(ProcedureVo procedureVo);

    void callMemberLogout(ProcedureVo procedureVo);

    List<?> callMemberSignUp(ProcedureVo procedureVo);
}
