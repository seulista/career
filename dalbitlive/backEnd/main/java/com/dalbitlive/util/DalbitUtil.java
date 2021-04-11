package com.dalbitlive.util;

import com.dalbitlive.common.code.Code;
import com.dalbitlive.common.code.Status;
import com.dalbitlive.common.vo.DeviceVo;
import com.dalbitlive.common.vo.ValidationResultVo;
import com.dalbitlive.common.vo.param.SelfAuthChkVo;
import com.dalbitlive.exception.GlobalException;
import com.dalbitlive.exception.service.ExceptionService;
import com.dalbitlive.exception.vo.db.DBErrorVo;
import com.dalbitlive.member.vo.TokenVo;
import com.google.gson.Gson;
import com.icert.comm.secu.IcertSecuManager;
import com.opentok.*;
import com.opentok.exception.OpenTokException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DalbitUtil {

    private static Environment environment;
    private static JwtUtil jwtUtil;
    private static ExceptionService exceptionService;
    private static LoginUtil loginUtil;
    private static MessageSource messageSource;

    @Autowired
    private Environment activeEnvironment;
    @Autowired
    private JwtUtil getJwtUtil;
    @Autowired
    private ExceptionService getExceptionService;
    @Autowired LoginUtil getLoginUtil;
    @Autowired
    private MessageSource getMessageSource;

    @PostConstruct
    private void init () {
        environment = this.activeEnvironment;
        jwtUtil = this.getJwtUtil;
        exceptionService = this.getExceptionService;
        loginUtil = this.getLoginUtil;
        messageSource = this.getMessageSource;
    }

    /**
     * 문자열에 값이 있는지 체크
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 리스트 값이 있는지 체크
     * @param list
     * @return
     */
    public static boolean isEmpty(List list){

        if(null == list){
            return true;
        }

        if(list != null && list.size() != 0){
            return false;
        }
        return 0 < list.size() ? false : true;
    }

    public static boolean isEmpty(String[] arr){
        try{
            if(arr != null && arr.length != 0){
                return false;
            }
            return 0 < arr.length ? false : true;
        }catch (NullPointerException e){
            return true;
        }

    }

    /**
     * Object 값이 있는지 체크
     * @param object
     * @return
     */
    public static boolean isEmpty(Object object) {
        return object != null ? false : true;
    }

    /**
     * 입력된 날짜가 유효한 날짜인지를 검사한다.
     * @param date 기준일자
     * @return 유효여부(true/false)
     */
    public static boolean isDate( String date ) {
        return isDate( date , null );
    }

    public static boolean isDate( String date , String format ) {
        if( date == null )
            return false;
        if( format == null )
            format = "yyyyMMdd";
        DateFormat df = new SimpleDateFormat( format , Locale.KOREA );
        df.setLenient( false );
        date = date.replaceAll( "\\D" , "" );
        try {
            df.parse( date );
            return true;
        }catch( ParseException pe ) {
            return false;
        }catch( Exception e ) {
            return false;
        }
    }

    /**
     * <p>오라클의 decode 함수와 동일한 기능을 가진 메서드이다.
     * <code>sourStr</code>과 <code>compareStr</code>의 값이 같으면
     * <code>returStr</code>을 반환하며, 다르면  <code>defaultStr</code>을 반환한다.
     * </p>
     *
     * <pre>
     * StringUtil.decode(null, null, "foo", "bar")= "foo"
     * StringUtil.decode("", null, "foo", "bar") = "bar"
     * StringUtil.decode(null, "", "foo", "bar") = "bar"
     * StringUtil.decode("하이", "하이", null, "bar") = null
     * StringUtil.decode("하이", "하이  ", "foo", null) = null
     * StringUtil.decode("하이", "하이", "foo", "bar") = "foo"
     * StringUtil.decode("하이", "하이  ", "foo", "bar") = "bar"
     * </pre>
     *
     * @param sourceStr 비교할 문자열
     * @param compareStr 비교 대상 문자열
     * @param returnStr sourceStr와 compareStr의 값이 같을 때 반환할 문자열
     * @param defaultStr sourceStr와 compareStr의 값이 다를 때 반환할 문자열
     * @return sourceStr과 compareStr의 값이 동일(equal)할 때 returnStr을 반환하며,
     *         <br/>다르면 defaultStr을 반환한다.
     */
    public static String decode(String sourceStr, String compareStr, String returnStr, String defaultStr) throws NullPointerException{
        if (sourceStr == null && compareStr == null) {
            return returnStr;
        }

        if (sourceStr == null && compareStr != null) {
            return defaultStr;
        }

        if (sourceStr.trim().equals(compareStr)) {
            return returnStr;
        }

        return defaultStr;
    }

    /**
     * <p>오라클의 decode 함수와 동일한 기능을 가진 메서드이다.
     * <code>sourStr</code>과 <code>compareStr</code>의 값이 같으면
     * <code>returStr</code>을 반환하며, 다르면  <code>sourceStr</code>을 반환한다.
     * </p>
     *
     * <pre>
     * StringUtil.decode(null, null, "foo") = "foo"
     * StringUtil.decode("", null, "foo") = ""
     * StringUtil.decode(null, "", "foo") = null
     * StringUtil.decode("하이", "하이", "foo") = "foo"
     * StringUtil.decode("하이", "바이", "foo") = "하이"
     * </pre>
     *
     * @param sourceStr 비교할 문자열
     * @param compareStr 비교 대상 문자열
     * @param returnStr sourceStr와 compareStr의 값이 같을 때 반환할 문자열
     * @return sourceStr과 compareStr의 값이 동일(equal)할 때 returnStr을 반환하며,
     *         <br/>다르면 sourceStr을 반환한다.
     */
    public static String decode(String sourceStr, String compareStr, String returnStr) throws NullPointerException{
        return decode(sourceStr, compareStr, returnStr, sourceStr);
    }

    /**
     * 객체가 null인지 확인하고 null인 경우 "" 로 바꾸는 메서드
     * @param object 원본 객체
     * @return resultVal 문자열
     */
    public static String isNullToString(Object object) {
        String string = "";

        if (!isEmpty(object)) {
            string = object.toString().trim();
        }

        return string;
    }

    public static int isStringToNumber(String str){
        return isStringToNumber(str, 0);
    }

    public static int isStringToNumber(String str, int nullValue){
        try {
            return DalbitUtil.isEmpty(str) ? nullValue : Integer.parseInt(str);
        } catch (Exception e){
            return nullValue;
        }
    }

  /**
     * 응용어플리케이션에서 고유값을 사용하기 위해 시스템에서17자리의TIMESTAMP값을 구하는 기능
     *
     * @param
     * @return Timestamp 값
     * @see
     */
    public static String getTimeStamp() {

        String pattern = "yyyyMMddHHmmssSSS";

        SimpleDateFormat sdfCurrent = new SimpleDateFormat(pattern, LocaleContextHolder.getLocale());
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        String rtnStr = sdfCurrent.format(ts.getTime());

        return rtnStr;
    }

    public static Date getDateMap(HashMap map, String key){
        try{
            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = transFormat.parse(map.get(key).toString());
            return date;
        }catch (Exception e){
            log.debug("StringUtil.getDateMap error - key name is [{}]", key);
            return null;
        }
    }

    public static String getStringMap(HashMap map, String key){
        try{
            return map.get(key).toString();
        }catch (Exception e){
            log.debug("StringUtil.getStringMap error - key name is [{}]", key);
            return "";
        }
    }

    public static int getIntMap(HashMap map, String key) {
        try{
            return (int) Math.floor(getDoubleMap(map, key));
        }catch (Exception e){
            log.debug("StringUtil.getIntMap error - key name is [{}]", key);
            return 0;
        }
    }


    public static long getLongMap(HashMap map, String key) {
        try{
            return (long) Math.floor(getDoubleMap(map, key));
        }catch (Exception e){
            log.debug("StringUtil.getIntMap error - key name is [{}]", key);
            return 0;
        }
    }

    public static double getDoubleMap(HashMap map, String key){
        try{
            return Double.valueOf(getStringMap(map, key));
        }catch (Exception e){
            log.debug("StringUtil.getDoubleMap error - key name is [{}]", key);
            return 0.0;
        }
    }

    public static boolean getBooleanMap(HashMap map, String key) {
        try{
            return Boolean.valueOf(getStringMap(map, key));
        }catch (Exception e){
            log.debug("StringUtil.getBooleanMap error - key name is [{}]", key);
            return false;
        }
    }

    public static String getProperty(String key){
        try{
            return environment.getProperty(key);
        }catch(Exception e){
            return "";
        }
    }

    /**
     * IP 주소 가져오기
     */
    public static String getIp(HttpServletRequest request){
        String clientIp = request.getHeader("Proxy-Client-IP");
        if (clientIp == null) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
            if (clientIp == null) {
                clientIp = request.getHeader("X-Forwarded-For");
                if (clientIp == null) {
                    clientIp = request.getRemoteAddr();
                }
            }
        }
        return clientIp;
    }

    public static String getServerIp(){
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        }catch (Exception e){
            return "";
        }
    }

    public static String setTimestampInJsonOutputVo(){
        StringBuffer sb = new StringBuffer();
        sb.append(getTimeStamp());

        String serverIp = getServerIp();
        if(!isEmpty(serverIp)){
            sb.append("_");
            sb.append(serverIp.substring(serverIp.lastIndexOf(".")+1));
        }

        sb.append("_");
        sb.append(DalbitUtil.getActiveProfile());
        return sb.toString();
    }

   /**
     * 이미지 path 경로 치환
     */
    public static String replacePath(String path){
        return path.replace(Code.포토_이미지_임시경로.getCode(), Code.포토_이미지_경로.getCode());
    }

    /**
     * 이미지 Done path 경로 치환
     */
    public static String replaceDonePath(String path){
        return path.replace(Code.포토_이미지_경로.getCode(), Code.포토_이미지_임시경로.getCode());
    }

    /**
     * UTC 로 변경
     *
     * @param dt
     * @return
     */
    public static LocalDateTime getUTC(Date dt){
        return dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime getUTC(String dt) {
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            return getUTC(transFormat.parse(dt));
        }catch(ParseException e){}
        return null;
    }

    /**
     * UTC기준 날짜 문자 변환
     *
     * @param dt
     * @return
     */
    public static String getUTCFormat(Date dt){
        return getUTC(dt).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String getUTCFormat(String dt){
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            return getUTCFormat(transFormat.parse(dt));
        }catch(ParseException e){}
        return null;
    }

    public static String convertDateFormat(Date date, String format){
        format = isEmpty(format) ? "yyyyMMddHHmmss" : format;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    /**
     * UTC기준 타임스탬프 변환
     *
     * @param dt
     * @return
     */
    public static long getUTCTimeStamp(Date dt){
        return Timestamp.valueOf(getUTC(dt)).getTime() / 1000;
    }
    public static long getUTCTimeStamp(String dt){
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            return getUTCTimeStamp(transFormat.parse(dt));
        }catch(ParseException e){}
        return 0;
    }

    public static boolean isEmptyHeaderAuthToken(String header){
        return isEmpty(header) || "undefined".equals(header);
    }

    public static boolean isAnonymousUser(Object principal){
        return isEmpty(principal) || "anonymousUser".equals(principal);
    }

    /**
     * Validation 체크
     */
    public static void throwValidaionException(BindingResult bindingResult, String methodName) throws GlobalException {

        ValidationResultVo validationResultVo = new ValidationResultVo();
        if(bindingResult.hasErrors()){
            validationResultVo.setSuccess(false);
            List errorList = bindingResult.getAllErrors();

            ArrayList bindingMessageList = new ArrayList();
            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setBasename("classpath:messages/validation");
            messageSource.setDefaultEncoding("UTF-8");
            messageSource.setUseCodeAsDefaultMessage(true);

            for (int i=0; i<bindingResult.getErrorCount(); i++) {
                FieldError fieldError = (FieldError) errorList.get(i);
                String message = null;
                if("Password".equals(fieldError.getCode())) {
                    message = fieldError.getDefaultMessage();
                }else {
                    String fieldName = "";
                    String field = fieldError.getDefaultMessage();
                    if (!isEmpty(field)) {
                        try {
                            HashMap<String, String> fieldMap = new Gson().fromJson(field, HashMap.class);
                            if (!isEmpty(fieldMap) && fieldMap.containsKey(LocaleContextHolder.getLocale().toString())) {
                                fieldName = getStringMap(fieldMap, LocaleContextHolder.getLocale().toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (!isEmpty(fieldName)) {
                        String validation_message_key = "";
                        List argList = new ArrayList();
                        argList.add(fieldName);
                        if ("NotBlank".equals(fieldError.getCode())) {
                            validation_message_key="validation.not_blank";
                        } else if ("NotNull".equals(fieldError.getCode())) {
                            validation_message_key="validation.not_blank";
                        } else if ("Min".equals(fieldError.getCode())) {
                            if(fieldError.getArguments().length == 2){
                                validation_message_key="validation.min";
                                argList.add(fieldError.getArguments()[1]);
                            }
                        } else if ("Max".equals(fieldError.getCode())) {
                            if(fieldError.getArguments().length == 2){
                                validation_message_key="validation.max";
                                argList.add(fieldError.getArguments()[1]);
                            }
                        } else if ("Size".equals(fieldError.getCode())) {
                            if(fieldError.getArguments().length == 3) {
                                if((int)fieldError.getArguments()[1] < 2147483647 && (int)fieldError.getArguments()[2] > 0 && (int)fieldError.getArguments()[1] == (int)fieldError.getArguments()[2]){
                                    validation_message_key="validation.size_same";
                                    argList.add(fieldError.getArguments()[1]);
                                }else if((int)fieldError.getArguments()[1] < 2147483647 && (int)fieldError.getArguments()[2] > 0 ){
                                    validation_message_key="validation.size";
                                    argList.add(fieldError.getArguments()[2]); //최소값
                                    argList.add(fieldError.getArguments()[1]); //최대값
                                }else if((int)fieldError.getArguments()[1] == 2147483647 && (int)fieldError.getArguments()[2] > 0){
                                    validation_message_key="validation.size_min";
                                    argList.add(fieldError.getArguments()[2]);
                                }else if((int)fieldError.getArguments()[1] < 2147483647 && (int)fieldError.getArguments()[2] == 0){
                                    validation_message_key="validation.size_max";
                                    argList.add(fieldError.getArguments()[1]);
                                }
                            }
                        } else if ("Pattern".equals(fieldError.getCode())) {
                            validation_message_key="validation.pattern";
                            argList.add(fieldError.getArguments()[1]);
                        }
                        if (!isEmpty(validation_message_key)) {
                            message = messageSource.getMessage(validation_message_key, argList.toArray(), LocaleContextHolder.getLocale());
                        }
                    }

                    if (isEmpty(message)) {
                        message = "param : " + fieldError.getField() + ", value : " + fieldError.getRejectedValue() + ", message : " + fieldError.getDefaultMessage();
                    }
                }
                bindingMessageList.add(message);
            }
            validationResultVo.setValidationMessageDetail(bindingMessageList);
            throw new GlobalException(Status.벨리데이션체크, null, validationResultVo == null ? new ArrayList() : validationResultVo.getValidationMessageDetail(), methodName, true);
        }

    }

    /**
     * 비밀번호 체크
     */
    public static Boolean isPasswordCheck(String password){

        if(DalbitUtil.isEmpty(password)){
            return true;
        }

        boolean isPattern = false;

        String pwPattern_1 = "^[A-Za-z[0-9]]{8,20}$";                         //영문 + 숫자
        String pwPattern_2 = "^[[0-9]!@#$%^&*()\\-_=+{};:,<.>]{8,20}$";       //숫자 + 특수문자
        String pwPattern_3 = "^[[A-Za-z]!@#$%^&*()\\-_=+{};:,<.>]{8,20}$";    //영문 + 특수문자
        String pwPattern_4 = "^[A-Za-z[0-9]!@#$%^&*()\\-_=+{};:,<.>]{8,20}$";  //영문 + 숫자 + 특수문자

        if(Pattern.matches(pwPattern_1, password) || Pattern.matches(pwPattern_2, password) || Pattern.matches(pwPattern_3, password) || Pattern.matches(pwPattern_4, password)){
            isPattern = true;
        }
        return isPattern;
    }

    /**
     * CORS 방지를 위한 Response 헤더 세팅
     * @param request
     * @param response
     */
    public static void setHeader(HttpServletRequest request, HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,"+DalbitUtil.getProperty("sso.header.cookie.name")+","+DalbitUtil.getProperty("rest.custom.header.name")+",redirectUrl,Proxy-Client-IP,WL-Proxy-Client-IP,X-Forwarded-For");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Content-Type", "application/json; charset=UTF-8");
    }

    public static String getActiveProfile(){
        return environment.getActiveProfiles()[0];
    }

    public static String remove(String str, char remove) throws NullPointerException{
        if (isEmpty(str) || str.indexOf(remove) == -1) {
            return str;
        }
        char[] chars = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != remove) {
                chars[pos++] = chars[i];
            }
        }
        return new String(chars, 0, pos);
    }

    public static String validChkDate(String dateStr) {
        String _dateStr = dateStr;

        if (dateStr == null || !(dateStr.trim().length() == 8 || dateStr.trim().length() == 10)) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
        if (dateStr.length() == 10) {
            _dateStr = remove(dateStr, '-');
        }
        return _dateStr;
    }

    public static String validChkTime(String timeStr) {
        String _timeStr = timeStr;

        if (_timeStr.length() == 5) {
            _timeStr = remove(_timeStr, ':');
        }
        if (_timeStr == null || !(_timeStr.trim().length() == 4)) {
            throw new IllegalArgumentException("Invalid time format: " + _timeStr);
        }

        return _timeStr;
    }

    /**
     * 브라우저 정보 가져오기
     */
    public static String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String browser;
        if (userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("Trident") > -1) {
            browser = "MSIE";
        } else if (userAgent.indexOf("Opera") > -1) {
            browser =  "Opera";
        } else if (userAgent.indexOf("Firefox") > -1) {
            browser = "Firefox";
        } else if (userAgent.indexOf("Edg") > -1) {
            browser = "Edge";
        } else if (userAgent.indexOf("Chrome") > -1) {
            DeviceVo deviceVo = new DeviceVo(request);
            if(deviceVo.getOs() == 1){
                browser = "WebView";
            }else{
                browser = "Chrome";
            }
        } else if (userAgent.indexOf("Safari") > -1) {
            browser = "Safari";
        }else if (userAgent.indexOf("AppleWebKit") > -1) { //웹뷰
            browser = "WebView";
        }else {
            browser = "Firefox";
        }
        return browser;
    }

    /**
     * 금지어 포함 여부
     * str: 금지어 문자열, param: 파라미터
     */
    public static Boolean isStringMatchCheck(String str, String param){
        boolean isMatch = false;
        str = str.replaceAll("\\|\\|", "\\|");
        str = str.replace("?", "0X01");
        str = str.replace("+", "0X02");
        str = str.replace("*", "0X03");
        str = str.replace(".", "0X04");
        str = str.replace("^", "0X05");
        str = str.replace("(", "0X06");
        str = str.replace(")", "0X07");
        str = str.replace("[", "0X08");
        str = str.replace("]", "0X09");
        str = str.replace("{", "0X10");
        str = str.replace("}", "0X11");
        str = str.replace("'", "0X12");
        str = str.replace("\"", "0X13");
        str = str.replace("\\\\", "0X14");

        param = param.replaceAll("\\|\\|", "\\|");
        param = param.replace("?", "0X01");
        param = param.replace("+", "0X02");
        param = param.replace("*", "0X03");
        param = param.replace(".", "0X04");
        param = param.replace("^", "0X05");
        param = param.replace("(", "0X06");
        param = param.replace(")", "0X07");
        param = param.replace("[", "0X08");
        param = param.replace("]", "0X09");
        param = param.replace("{", "0X10");
        param = param.replace("}", "0X11");
        param = param.replace("'", "0X12");
        param = param.replace("\"", "0X13");
        param = param.replace("\\\\", "0X14");

        try {
            Pattern p = Pattern.compile(str, Pattern.CASE_INSENSITIVE); //대소문자 구분안함
            Matcher m = p.matcher(param);
            while (m.find()){
                return isMatch = true;
            }
        } catch (Exception e){
            log.error("금지어 체크 오류 isStringMatchCheck");

            try {
                DBErrorVo errorLogVo = new DBErrorVo();
                errorLogVo.setMem_no("99999999999999");
                errorLogVo.setOs("API");
                errorLogVo.setVersion("");
                errorLogVo.setBuild("");
                errorLogVo.setDtype("banWord");
                errorLogVo.setCtype("금지어 체크 오류");
                errorLogVo.setDesc(param);
                exceptionService.saveErrorLog(errorLogVo);
            } catch (Exception e1){}

            return isMatch = true;
        }

        return isMatch;
    }

    /**
     * 금지어 *** 변환
     * str: 금지어 문자열, param: 파라미터
     */
    public static String replaceMaskString(String str, String param){
        str = str.replaceAll("\\|\\|", "\\|");
        str = str.replaceAll("\\|\\|", "\\|");
        str = str.replace("?", "0X01");
        str = str.replace("+", "0X02");
        str = str.replace("*", "0X03");
        str = str.replace(".", "0X04");
        str = str.replace("^", "0X05");
        str = str.replace("(", "0X06");
        str = str.replace(")", "0X07");
        str = str.replace("[", "0X08");
        str = str.replace("]", "0X09");
        str = str.replace("{", "0X10");
        str = str.replace("}", "0X11");
        str = str.replace("'", "0X12");
        str = str.replace("\"", "0X13");
        str = str.replace("\\\\", "0X14");

        param = param.replaceAll("\\|\\|", "\\|");
        param = param.replace("?", "0X01");
        param = param.replace("+", "0X02");
        param = param.replace("*", "0X03");
        param = param.replace(".", "0X04");
        param = param.replace("^", "0X05");
        param = param.replace("(", "0X06");
        param = param.replace(")", "0X07");
        param = param.replace("[", "0X08");
        param = param.replace("]", "0X09");
        param = param.replace("{", "0X10");
        param = param.replace("}", "0X11");
        param = param.replace("'", "0X12");
        param = param.replace("\"", "0X13");
        param = param.replace("\\\\", "0X14");

        StringBuffer sb = new StringBuffer();
        try {
            Pattern p = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(param);
            while (m.find()){
                m.appendReplacement(sb, maskWord(m.group()));
            }
            m.appendTail(sb);
        } catch (Exception e){
            sb.setLength(0);
            sb.append(param);
            log.error("금지어 변환 오류 replaceMaskString");

            try{
                DBErrorVo errorLogVo = new DBErrorVo();
                errorLogVo.setMem_no("99999999999999");
                errorLogVo.setOs("API");
                errorLogVo.setVersion("");
                errorLogVo.setBuild("");
                errorLogVo.setDtype("banWord");
                errorLogVo.setCtype("금지어 변환 오류");
                errorLogVo.setDesc(param);
                exceptionService.saveErrorLog(errorLogVo);
            } catch (Exception e1){
            }

        }

        return sb.toString().replaceAll("0X01", "?")
                            .replaceAll("0X02", "+")
                            .replaceAll("0X03", "*")
                            .replaceAll("0X04", ".")
                            .replaceAll("0X05", "^")
                            .replaceAll("0X06", "(")
                            .replaceAll("0X07", ")")
                            .replaceAll("0X08", "[")
                            .replaceAll("0X09", "]")
                            .replaceAll("0X10", "{")
                            .replaceAll("0X11", "}")
                            .replaceAll("0X12", "'")
                            .replaceAll("0X13", "\"")
                            .replaceAll("0X14", "\\\\");
    }

    /**
     * 마스킹 유틸
     */
    public static String maskWord(String word) {
        StringBuffer buff = new StringBuffer();
        char[] ch = word.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            /*if (i < 1) {
                buff.append(ch[i]);
            } else {
                buff.append("*");
            }*/
            buff.append("*");
        }
        return buff.toString();
    }

    public static boolean versionCompare(String str1, String str2) {
        try ( Scanner s1 = new Scanner(str1);
              Scanner s2 = new Scanner(str2);) {
            s1.useDelimiter("\\.");
            s2.useDelimiter("\\.");

            while (s1.hasNextInt() && s2.hasNextInt()) {
                int v1 = s1.nextInt();
                int v2 = s2.nextInt();
                if (v1 < v2) {
                    return false;
                } else if (v1 > v2) {
                    return true;
                }
            }

            if (s1.hasNextInt() && s1.nextInt() != 0)
                return true; //str1 has an additional lower-level version number
            if (s2.hasNextInt() && s2.nextInt() != 0)
                return false; //str2 has an additional lower-level version

            return false;
        }
    }

    public static String getMessage(String code){
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
