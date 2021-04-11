package com.dalbitlive.member.vo;

import com.dalbitlive.common.vo.ImageVo;
import com.dalbitlive.member.vo.db.DBLoginInfoOutVo;
import com.dalbitlive.member.vo.db.DBProfImgOutVo;
import com.dalbitlive.member.vo.out.OutProgImgVo;
import com.dalbitlive.util.DalbitUtil;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class TokenVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String authToken;
    private boolean isLogin;

    private String memNo;
    private String  nickNm;
    private String  gender;
    private int     age;
    private String  birth;
    private String  memId;
    private ImageVo profImg = new ImageVo();
    private String  profMsg;
    private int     dalCnt = 0;
    private int     byeolCnt = 0;
    private int badgeSpecial = 0;
    private boolean isMailboxOn = true;
    private boolean isCert = false;
    private boolean isAdmin = false;
    private List<OutProgImgVo> profImgList = new ArrayList<>();

    public TokenVo(){}
    public TokenVo(String authToken, String memNo, boolean isLogin){
        this.authToken = authToken;
        this.memNo = memNo;
        this.isLogin = isLogin;
    }

    public TokenVo(DBLoginInfoOutVo dbLoginInfoOutVo, List<DBProfImgOutVo> profImgList){
        this.memNo = dbLoginInfoOutVo.getMemNo();
        this.isLogin = DalbitUtil.isLogin(this.memNo);
        this.nickNm = dbLoginInfoOutVo.getNickNm();
        this.gender = dbLoginInfoOutVo.getGender();
        this.birth = dbLoginInfoOutVo.getBirth();
        this.age = dbLoginInfoOutVo.getAge();
        this.memId = dbLoginInfoOutVo.getMemId();
        this.badgeSpecial = dbLoginInfoOutVo.getBadgeSpecial();
        this.profImg = new ImageVo(dbLoginInfoOutVo.getProfImgPath(), this.gender, DalbitUtil.getProperty("server.photo.url"));
        this.profMsg = dbLoginInfoOutVo.getProfMsg();
        this.dalCnt = dbLoginInfoOutVo.getDalCnt();
        this.byeolCnt = dbLoginInfoOutVo.getByeolCnt();
        this.isCert = dbLoginInfoOutVo.isCert();
        this.isMailboxOn = dbLoginInfoOutVo.isMailboxOn();
        this.isAdmin = dbLoginInfoOutVo.isAdmin();
        if(!DalbitUtil.isEmpty(profImgList)){
            for(DBProfImgOutVo img : profImgList){
                this.profImgList.add(new OutProgImgVo(img));
            }
        }
    }
}
