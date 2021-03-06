package com.dalbitlive.common.vo;

import com.dalbitlive.util.CookieUtil;
import com.dalbitlive.util.DalbitUtil;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;

@Getter
@Setter
public class DeviceVo {
    private int os;
    private String deviceUuid;
    private String deviceToken;
    private String appVersion;
    private String appBuild;
    private String adId;
    private String ip;
    private String isHybrid;
    private String deviceManufacturer;
    private String deviceModel;
    private String sdkVersion;
    private String isFirst;

    public DeviceVo(HttpServletRequest request){
        this.os = DalbitUtil.convertRequestParamToInteger(request,"os");
        this.deviceUuid = DalbitUtil.convertRequestParamToString(request,"deviceId");
        this.deviceToken = DalbitUtil.convertRequestParamToString(request,"deviceToken");
        this.appVersion = DalbitUtil.convertRequestParamToString(request,"appVer");
        this.appBuild = DalbitUtil.convertRequestParamToString(request,"appBuild");
        this.adId = DalbitUtil.convertRequestParamToString(request,"appAdId");
        this.ip = DalbitUtil.getIp(request);
        this.isHybrid = DalbitUtil.convertRequestParamToString(request, "isHybrid");
        this.deviceManufacturer = DalbitUtil.convertRequestParamToString(request, "deviceManufacturer");
        this.deviceModel = DalbitUtil.convertRequestParamToString(request, "deviceModel");
        this.sdkVersion = DalbitUtil.convertRequestParamToString(request, "deviceSdkVersion");

        if(DalbitUtil.isEmpty(this.appVersion)){
            appVersion = DalbitUtil.convertRequestParamToString(request,"appVersion");
        }

        String headerName = DalbitUtil.getProperty("rest.custom.header.name");
        if(!DalbitUtil.isEmpty(headerName)){
            String customHeader = request.getHeader(headerName);
            if(customHeader != null && !"".equals(customHeader.trim())){
                customHeader = URLDecoder.decode(customHeader);
                try {
                    HashMap<String, Object> headers = new Gson().fromJson(customHeader, HashMap.class);
                    if (!DalbitUtil.isEmpty(headers) && !DalbitUtil.isEmpty(headers.get("os"))) {
                        if (this.os == -1) {
                            this.os = (int) DalbitUtil.getDoubleMap(headers, "os");
                        }
                        if (DalbitUtil.isEmpty(this.deviceUuid)) {
                            this.deviceUuid = DalbitUtil.getStringMap(headers, "deviceId");
                        }
                        if (DalbitUtil.isEmpty(this.deviceToken)) {
                            this.deviceToken = DalbitUtil.getStringMap(headers, "deviceToken");
                        }
                        if (DalbitUtil.isEmpty(this.appVersion)) {
                            this.appVersion = DalbitUtil.getStringMap(headers, "appVer");
                            if (DalbitUtil.isEmpty(this.appVersion)) {
                                this.appVersion = DalbitUtil.getStringMap(headers, "appVersion");
                            }
                        }
                        if (DalbitUtil.isEmpty(this.adId)) {
                            this.adId = DalbitUtil.getStringMap(headers, "appAdId");
                        }
                        if (DalbitUtil.isEmpty(this.adId)) {
                            this.isHybrid = DalbitUtil.getStringMap(headers, "isHybrid");
                        }
                        if (DalbitUtil.isEmpty(this.appBuild)) {
                            String appBulid = DalbitUtil.getStringMap(headers, "appBuild");
                            this.appBuild = appBulid;
                        }
                        if (DalbitUtil.isEmpty(this.deviceManufacturer)) {
                            this.deviceManufacturer = DalbitUtil.getStringMap(headers, "deviceManufacturer");
                        }
                        if (DalbitUtil.isEmpty(this.deviceModel)) {
                            this.deviceModel = DalbitUtil.getStringMap(headers, "deviceModel");
                        }
                        if (DalbitUtil.isEmpty(this.sdkVersion)) {
                            this.sdkVersion = DalbitUtil.getStringMap(headers, "deviceSdkVersion");
                        }
                        if (DalbitUtil.isEmpty(this.isFirst)) {
                            this.isFirst = DalbitUtil.getStringMap(headers, "isFirst");
                        }
                    }
                }catch(Exception e){}
            }else{
                // ??????????????? ?????? ????????? ??? ??????
                String cookieHeader = "";
                try{
                    CookieUtil cookieUtil = new CookieUtil(request);
                    cookieHeader = cookieUtil.getValue(headerName);
                }catch(IOException e){}
                if(!DalbitUtil.isEmpty(cookieHeader)){
                    try {
                        HashMap<String, Object> headers = new Gson().fromJson(cookieHeader, HashMap.class);

                        if (!DalbitUtil.isEmpty(headers.get("os")) && !DalbitUtil.isEmpty(headers.get("deviceId"))) {
                            if (this.os == -1) {
                                this.os = (int) DalbitUtil.getDoubleMap(headers, "os");
                            }
                            if (DalbitUtil.isEmpty(this.deviceUuid)) {
                                this.deviceUuid = DalbitUtil.getStringMap(headers, "deviceId");
                            }
                            if (DalbitUtil.isEmpty(this.deviceToken)) {
                                this.deviceToken = DalbitUtil.getStringMap(headers, "deviceToken");
                            }
                            if (DalbitUtil.isEmpty(this.appVersion)) {
                                this.appVersion = DalbitUtil.getStringMap(headers, "appVer");
                                if (DalbitUtil.isEmpty(this.appVersion)) {
                                    this.appVersion = DalbitUtil.getStringMap(headers, "appVersion");
                                }
                            }
                            if (DalbitUtil.isEmpty(this.adId)) {
                                this.adId = DalbitUtil.getStringMap(headers, "appAdId");
                            }
                            if (DalbitUtil.isEmpty(this.adId)) {
                                this.isHybrid = DalbitUtil.getStringMap(headers, "isHybrid");
                            }
                            if (DalbitUtil.isEmpty(this.appBuild)) {
                                String appBulid = DalbitUtil.getStringMap(headers, "appBuild");
                                this.appBuild = appBulid;
                            }
                            if (DalbitUtil.isEmpty(this.deviceManufacturer)) {
                                this.deviceManufacturer = DalbitUtil.getStringMap(headers, "deviceManufacturer");
                            }
                            if (DalbitUtil.isEmpty(this.deviceModel)) {
                                this.deviceModel = DalbitUtil.getStringMap(headers, "deviceModel");
                            }
                            if (DalbitUtil.isEmpty(this.sdkVersion)) {
                                this.sdkVersion = DalbitUtil.getStringMap(headers, "deviceSdkVersion");
                            }
                            if (DalbitUtil.isEmpty(this.isFirst)) {
                                this.isFirst = DalbitUtil.getStringMap(headers, "isFirst");
                            }
                        }
                    }catch (Exception e){}
                }
            }
        }

        this.deviceToken = DalbitUtil.isNullToString(deviceToken);
        this.appVersion = DalbitUtil.isNullToString(appVersion);
        this.adId = DalbitUtil.isNullToString(adId);

        if(!DalbitUtil.isEmpty(this.appBuild) && this.appBuild.indexOf(".") > -1){
            this.appBuild = this.appBuild.substring(0, this.appBuild.indexOf("."));
        }
        if("192.168.10.163".equals(this.ip) || "192.168.10.164".equals(this.ip)){ //????????????????????? 3????????? ??????
            this.os =3;
        }
    }
}
