package com.dalbitlive.member.vo.db;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class DBLogoutOutVo {
    private String spName;
    private String desc;
    private List<DBLogoutRoomOutVo> roomList;
}
