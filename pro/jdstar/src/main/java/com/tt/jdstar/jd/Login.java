package com.tt.jdstar.jd;

import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.tt.jdstar.http.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Login {

    private static Logger logger = LoggerFactory.getLogger(Login.class);

    static String venderId = "";
    static Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>(16);
    static String ticket = "";

    public static String Login() throws IOException, URISyntaxException, InterruptedException {
        JSONObject headers = new JSONObject();
        headers.put(Start.headerAgent, Start.headerAgentArg);
        headers.put(Start.Referer, Start.RefererArg);
        //获取二维码
        Long now = System.currentTimeMillis();
        String qrCodeUrl = "https://qr.m.jd.com/show?appid=133&size=147&t=" + now;

        boolean b = HttpClientUtil.doGetDownloadFile(headers, qrCodeUrl);
        logger.info("下载京东二维码："+b);

        CookieStore cookieStore = HttpClientUtil.cookieStore;
        List<Cookie> cookieList = cookieStore.getCookies();
        logger.info("开始执行获取token");
        String token ="";
        String cookieStr = "";
        if (!cookieList.isEmpty()){
            for (int i = 0; i < cookieList.size(); i++) {
                logger.info(cookieList.get(i).getName());
                cookieStr += cookieList.get(i).getName() +"="+cookieList.get(i).getValue()+";";
                if(cookieList.get(i).toString().indexOf("wlfstk_smdl")>-1){
                    String sss = cookieList.get(i).toString().substring(cookieList.get(i).toString().indexOf("value:")+7,cookieList.get(i).toString().length());
                    token =sss.substring(0,sss.indexOf("]"));
                    logger.info("token:"+token);
                }
            }
        }

        headers.put("Cookie", cookieStr);
        //判断是否扫二维码
        while (true) {
            String checkUrl = "https://qr.m.jd.com/check?appid=133&callback=jQuery" + (int) ((Math.random() * (9999999 - 1000000 + 1)) + 1000000) + "&token=" + token + "&_=" + System.currentTimeMillis();
            String qrCode = HttpClientUtil.doGet(headers, checkUrl);
            if (qrCode.indexOf("二维码未扫描") != -1) {
                logger.info("二维码未扫描，请扫描二维码登录");
            } else if (qrCode.indexOf("请手机客户端确认登录") != -1) {
                logger.info("请手机客户端确认登录");
            } else if (qrCode.indexOf("二维码无效") != -1){
                logger.info("二维码无效");
            }else {
                ticket = qrCode.split("\"ticket\" : \"")[1].split("\"\n" +
                        "}\\)")[0];
                logger.info("已完成二维码扫描登录,ticket:{}",ticket);
                break;
            }
            Thread.sleep(3000);
        }
        //验证，获取登录后的cookie
        String loginCookie = "";
        String qrCodeTicketValidation = HttpClientUtil.doGet(headers, "https://passport.jd.com/uc/qrCodeTicketValidation?t=" + ticket);
        logger.info("登录后的结果:{}",qrCodeTicketValidation);

        CookieStore loginCookieStore = HttpClientUtil.cookieStore;
        List<Cookie> loginCookieList = loginCookieStore.getCookies();
        if (!loginCookieList.isEmpty()){
            for (int i = 0; i < loginCookieList.size(); i++) {
                logger.info(loginCookieList.get(i).getName());
                loginCookie += loginCookieList.get(i).getName() +"="+loginCookieList.get(i).getValue()+";";
            }
        }
        logger.info("登录后的cookie：",loginCookie);
        return loginCookie;
    }


}
