package com.dalbitlive.common.code;

import lombok.Getter;

@Getter
public enum Status {

    //공통
    생성금지("C100", "create.no", "방생성 금지"),
    공백("TEMP", "empty.success", "공백"),
    조회("C001", "read.success", "조회"),
    수정("C002", "update.success", "수정"),
    생성("C003", "create.success", "생성"),
    삭제("C004", "delete.success", "삭제"),
    파라미터오류("C005", "param.error", "파라미터 오류 시"),
    비즈니스로직오류("C006", "business.error", "비즈니스로직 오류 시"),
    벨리데이션체크("C007", "validation.error", "벨리데이션체크 오류 시"),
    부적절한문자열("C007", "string.error", "부적합한 기호 및 문자열 포함 시"),
    데이터없음("0", "no.data", "데이터가 없을 시"),
    로그인필요_성공("-98", "need.login.success", "로그인 필요 시"),
    로그인필요("-99", "need.login", "로그인 필요 시"),
    이전작업대기중("-97", "ready.to.prev.process", "동일작업 중복 호출"),
    사용자요청취소("C999", "client.abort.exception", "사용자 요청취소"),

    최신버전_업데이트_필요("100", "update.need", "업데이트 필요"),

    //차단관련 운영메시지
    차단_이용제한("0", "block.member.restriction", "운영자에 의해 차단되어 이용제한 시"),

    //로그인
    로그인성공("0", "login.success", "로그인 성공 시"),
    관리자로그인성공("0", "admin.login.success", "관리자 아이디로 로그인 성공 시"),
    로그인실패_회원가입필요("1", "login.join.need", "회원가입 필요 시"),
    로그인실패_패스워드틀림("-1", "login.fail", "로그인 실패 시 - 아이디/비밀번호가 틀릴 시"),
    로그인실패_파라메터이상("-2", "login.param.error", "로그인 실패 시 - 파라메터이상"),
    로그인실패_블럭상태("-3", "login.block", "로그인 실패 시 - 블럭상태"),
    로그인실패_탈퇴("-4", "login.kick.out", "로그인 실패 시 - 탈퇴"),
    로그인실패_영구정지("-5", "login.permanent.stop", "로그인 실패 시 - 영구정지"),
    로그인실패_청취방존재("-6", "login.connected.room", "로그인 실패 시 - 동일방 접속"),
    로그인실패_운영자차단("-7", "login.admin.block", "로그인 실패 시 - 운영자가 차단한 deviceUuid or IP 인 경우"),
    로그인실패_휴면상태("-8", "login.sleep", "로그인 실패 시 - 휴면상태"),
    로그인오류("C006", "login.error", "로그인 오류 시"),

    //로그아웃
    로그아웃성공("0", "logout.success", "로그아웃 성공 시"),
    로그아웃실패_진행중인방송("0", "logout.mybroadcast.ing", "로그아웃을 시도했지만 방송을 진행하고 있을 때"),
;



   final private String RESULT_SUCCESS = "success";
    final private String RESULT_FAIL = "fail";

    final private String result;
    final private String messageCode;
    final private String messageKey;
    final private String desc;

    Status(String messageCode, String messageKey, String desc){
        this.result = messageKey.contains("success") ? RESULT_SUCCESS : RESULT_FAIL;
        this.messageCode = messageCode;
        this.messageKey = messageKey;
        this.desc = desc;
    }
}
