package com.dalbitlive.member.vo.db;

import com.dalbitlive.common.vo.DBCommonVo;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DBLoginVo extends DBCommonVo {
    private String memSlct;
    private String id;
    private String pw;
    private int os;
    private String deviceUuid;
    private String deviceToken;
    private String appVersion;
    private String adId;
    private String location;
    private String ip;
    private String browser;
    private String buildVersion;

    private String room_no;
    private int auth;
    private String device_uuid;
    private int state;

    public DBLoginVo(){}

    //회원용
    public DBLoginVo(String memSlct, String id, String pw, int os, String deviceUuid, String deviceToken, String appVersion, String adId, String location, String ip, String browser, String appBuild){
        setMemSlct(memSlct);
        setId(id);
        setPw(pw);
        setOs(os);
        setDeviceUuid(deviceUuid);
        setDeviceToken(deviceToken);
        setAppVersion(appVersion);
        setAdId(adId);
        setLocation(location);
        setIp(ip);
        setBrowser(browser);
        setBuildVersion(appBuild);
    }

    //비회원용
    public DBLoginVo(String memSlct, int os, String deviceUuid, String deviceToken, String appVersion, String adId, String location, String ip, String browser){
        setMemSlct(memSlct);
        setOs(os);
        setDeviceUuid(deviceUuid);
        setDeviceToken(deviceToken);
        setAppVersion(appVersion);
        setAdId(adId);
        setLocation(location);
        setIp(ip);
        setBrowser(browser);
    }
}
