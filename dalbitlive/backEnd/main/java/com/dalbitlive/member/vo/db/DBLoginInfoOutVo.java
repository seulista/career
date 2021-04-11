package com.dalbitlive.member.vo.db;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DBLoginInfoOutVo {
    private String memNo;
    private String  gender;
    private String  nickNm;
    private String  birth;
    private int     age;
    private String  memId;
    private int badgeSpecial = 0;
    private String profImgPath;
    private String  profMsg;
    private int     dalCnt = 0;
    private int     byeolCnt = 0;
    private boolean isCert = false;
    private boolean isMailboxOn = true;
    private boolean isAdmin = false;
}
