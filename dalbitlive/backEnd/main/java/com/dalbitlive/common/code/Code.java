package com.dalbitlive.common.code;

import lombok.Getter;

@Getter
public enum Code {

    //포토 프로필 이미지 PREFIX
    포토_프로필_PREFIX("/profile_0", "포토서버 프로필 실제 경로 prefix"),
    포토_프로필_임시_PREFIX("/profile_1", "포토서버 프로필 임시 경로 prefix"),
    포토_프로필_썸네일_PREFIX("/profile_2", "포토서버 프로필 이미지 썸네일 prefix"),
    포토_프로필_디폴트_PREFIX("/profile_3", "포토서버 배경 이미지 배경 디폴트 prefix"),

    포토_이미지_경로("_0/", "실제 파일이 올라갈 경로 -done 이후 경로"),
    포토_이미지_임시경로("_1/", "이미지가 올라가는 임시 경로"),
   ;

    final private String code;
    final private String desc;

    Code(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
