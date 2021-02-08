package com.tt.jdstar.jd;

import com.alibaba.fastjson.JSONObject;
import com.tt.jdstar.http.HttpUrlConnectionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Start {

    private static Logger logger = LoggerFactory.getLogger(Start.class);

    final static String headerAgent = "User-Agent";
    final static String headerAgentArg = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";
    final static String Referer = "Referer";
    final static String RefererArg = "https://passport.jd.com/new/login.aspx?ReturnUrl=https%3A%2F%2Fwww.jd.com%2F";
    //商品id
    static String pid = "100012043978";
    //eid
    static String eid = "S5HVDQLVHH54D5Y6O6G54UUMBKEXORGKO25DQ75GH3O67ZAROXL7DPH3M26TY3MOG5J5TQ6ODEIZVRPTYO7RPMSIOU";
    //fp
    static String fp = "465d9960bf40fe27306b6e43e7577c4b";
    //抢购数量
    volatile static Integer ok = 2;

    static CookieManager manager = new CookieManager();


    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, ParseException {
        //登录
        String loginCookie = Login.Login();
        //判断是否开始抢购
        judgePruchase(loginCookie);
        //开始抢购
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < 1; i++) {
            threadPoolExecutor.execute(new RushToPurchase(loginCookie));
        }
//        new RushToPurchase(loginCookie).run();
    }

    public static void judgePruchase(String cookie) throws IOException, ParseException, InterruptedException {
        //获取开始时间
        JSONObject headers = new JSONObject();
        headers.put(Start.headerAgent, Start.headerAgentArg);
//        headers.put(Start.Referer, Start.RefererArg);
        headers.put("Cookie", cookie);
        JSONObject shopDetail = JSONObject.parseObject(HttpUrlConnectionUtil.get(headers, "https://item-soa.jd.com/getWareBusiness?skuId=" + pid));
        if (shopDetail.get("yuyueInfo") != null) {
            String buyDate = JSONObject.parseObject(shopDetail.get("yuyueInfo").toString()).get("buyTime").toString();
            String startDate = buyDate.split("-202")[0] + ":00";
            Long startTime = HttpUrlConnectionUtil.dateToTime(startDate);
            //开始抢购
            while (true) {
                //获取京东时间
                JSONObject jdTime = JSONObject.parseObject(HttpUrlConnectionUtil.get(headers, "https://a.jd.com//ajax/queryServerData.html"));
                Long serverTime = Long.valueOf(jdTime.get("serverTime").toString());
                if (startTime >= serverTime) {
                    System.out.println("正在等待抢购时间");
                    Thread.sleep(300);
                } else {
                    break;
                }
            }
        }
    }
}
