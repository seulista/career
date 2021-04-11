package com.dalbitlive.member.vo.out;

import com.dalbitlive.common.vo.ImageVo;
import com.dalbitlive.member.vo.db.DBProfImgOutVo;
import com.dalbitlive.util.DalbitUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter @Getter
public class OutProgImgVo implements Serializable {
    private ImageVo profImg = new ImageVo();
    private Boolean isLeader = false;
    private long idx = 0;

    public OutProgImgVo(DBProfImgOutVo imgOutVo){
        this.idx = imgOutVo.getIdx();
        this.isLeader = imgOutVo.getLeader_yn() == 1;
        this.profImg = new ImageVo(imgOutVo.getProfileImage(), imgOutVo.getMemSex(), DalbitUtil.getProperty("server.photo.url"));
    }
}
