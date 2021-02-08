package com.tt.jdstar.jd;

import com.alibaba.fastjson.JSONObject;
import com.tt.jdstar.http.HttpClientUtil;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RushToPurchase implements Runnable{
    private static Logger logger = LoggerFactory.getLogger(RushToPurchase.class);
    //请求头
    volatile static Integer times = 0;
    static Map<String, List<String>> stringListMap = new HashMap<String, List<String>>();

    public String loginCookie;

    public RushToPurchase(String loginCookie) {
        this.loginCookie = loginCookie;
    }

    public void run() {
        JSONObject headers = new JSONObject();
        while (times < Start.ok) {
            //获取ip，使用的是免费的 携趣代理 ，不需要或者不会用可以注释掉
            setIpProxy();

            headers.put(Start.headerAgent, Start.headerAgentArg);
            headers.put("Cookie",loginCookie);
            //抢购
            String gate = null;
            try {
                gate = HttpClientUtil.doGet(headers, "https://cart.jd.com/gate.action?pcount=1&ptype=1&pid=" + Start.pid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //订单信息
            String orderInfo = HttpClientUtil.doGet(headers, "https://trade.jd.com/shopping/order/getOrderInfo.action");
//            CookieStore cartCookieStore = HttpClientUtil.cookieStore;
//            List<Cookie> cartCookie = cartCookieStore.getCookies();
//            headers.put("Cookie", cartCookie);

            //提交订单
            JSONObject subData = new JSONObject();
            headers = new JSONObject();
            headers.put("Cookie",loginCookie);
            subData.put("overseaPurchaseCookies", "");
            subData.put("vendorRemarks", "[]");
            subData.put("submitOrderParam.sopNotPutInvoice", "false");
            subData.put("submitOrderParam.ignorePriceChange", "1");
            subData.put("submitOrderParam.btSupport", "0");
            subData.put("submitOrderParam.isBestCoupon", "1");
            subData.put("submitOrderParam.jxj", "1");
            subData.put("submitOrderParam.trackID", Login.ticket);
            subData.put("submitOrderParam.eid", Start.eid);
            subData.put("submitOrderParam.fp", Start.fp);
            subData.put("submitOrderParam.needCheck", "1");

            headers.put("Referer", "http://trade.jd.com/shopping/order/getOrderInfo.action");
            headers.put("origin", "https://trade.jd.com");
            headers.put("Content-Type", "application/json");
            headers.put("x-requested-with", "XMLHttpRequest");
            headers.put("upgrade-insecure-requests", "1");
            headers.put("sec-fetch-user", "?1");

            String submitOrder = null;
            try {
                if (times < Start.ok) {
                    submitOrder = HttpUrlConnectionUtil.post(headers, "https://trade.jd.com/shopping/order/submitOrder.action", null);
                    logger.info("submitOrder:{}",submitOrder);
                } else {
                    logger.info("已抢购" + Start.ok + "件，请尽快完成付款");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (submitOrder.contains("刷新太频繁了") || submitOrder.contains("抱歉，您访问的内容不存在")) {
                logger.info("刷新太频繁了,您访问的内容不存在");
                continue;
            }
            JSONObject jsonObject = JSONObject.parseObject(submitOrder);
            String success = null;
            String message = null;
            if (jsonObject != null && jsonObject.get("success") != null) {
                success = jsonObject.get("success").toString();
            }
            if (jsonObject != null && jsonObject.get("message") != null) {
                message = jsonObject.get("message").toString();
            }

            if (success == "true") {
                logger.info("抢购成功，请尽快完成付款");
                times++;
            } else {
                if (message != null) {
                    logger.info(message);
                } else if (submitOrder.contains("很遗憾没有抢到")) {
                    logger.info("很遗憾没有抢到，再接再厉哦");
                } else if (submitOrder.contains("抱歉，您提交过快，请稍后再提交订单！")) {
                    logger.info("抱歉，您提交过快，请稍后再提交订单！");
                } else if (submitOrder.contains("系统正在开小差，请重试~~")) {
                    logger.info("系统正在开小差，请重试~~");
                } else if (submitOrder.contains("您多次提交过快")) {
                    logger.info("您多次提交过快，请稍后再试");
                } else {
                    logger.info("获取用户订单信息失败");
                }
            }
        }
    }

    public static void setIpProxy() {
        String ip = null;
        try {
            ip = HttpUrlConnectionUtil.ips().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] r1 = ip.split(":");
        logger.info(ip);
        System.getProperties().setProperty("http.proxyHost", r1[0]);
        System.getProperties().setProperty("http.proxyPort", r1[1]);
        System.err.println(r1[0] + ":" + r1[1]);
    }
}
