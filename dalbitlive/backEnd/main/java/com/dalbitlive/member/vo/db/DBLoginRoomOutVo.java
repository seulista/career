package com.dalbitlive.member.vo.db;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DBLoginRoomOutVo {
    private String room_no;
    private int auth;
    private String device_uuid;
    private int state;
}
