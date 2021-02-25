package com.tt.jdstar.jd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tt.jdstar.http.HttpClientUtil;
import com.tt.jdstar.jd.vo.ItemDetailsVO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

public class Goods {

    /**
     * 获取商品详情页面
     * @param skuId
     * @return
     */
    private String getItemDetailPage(String skuId){
        String gUrl = String.format("https://item.jd.com/{}.html", skuId);
        Map<String,String> params = new HashMap<>();
        params.put("sku",skuId);
        String s = HttpClientUtil.sendHttpPost(gUrl);
        return s;
    }

    /**
     * 获取单个库存状态
     * @param skuId
     * @param num
     * @param area
     * @return
     */
    private ItemDetailsVO getItemDetail(String skuId,int num,String area){
        String productHtml = getItemDetailPage(skuId);
        Document document = Jsoup.parse(productHtml);

        ItemDetailsVO itemDetailsVO = new ItemDetailsVO();
        //获得商品标题
        if(document.select("div.sku-name").size()>0){
            String title = document.select("div.sku-name").get(0).text();
        }
        //获得商品品牌
        String brand = document.select("#parameter-brand li").attr("title");
        //获得商品名称
        String itemName = document.select("[class=parameter2 p-parameter-list] li:first-child").attr("title");
        //获取商品店铺
        String storageName = document.select("div.name a").attr("title");
        //获得店铺类别
        String storageType = document.select("[class=name goodshop EDropdown] em").text();
        //获取图片
        String imgUrl = document.select("#spec-img").attr("data-origin");
        //获得商品链接
        String itemUrl = "https://item.jd.com/" + skuId + ".html";
        //评论炸了  狗东  记得修复  不许刷水军

        //获取商品价格
        double itemPrice = getItemPrice(skuId);

        return itemDetailsVO;


    }

    /**
     * 获取商品价格
     * @param skuId
     * @return
     */
    private double getItemPrice(String skuId){
        String gUrl = String.format("https://p.3.cn/prices/mgets?skuIds=J_{}", skuId);
        String s = HttpClientUtil.sendHttpPost(gUrl);
        JSONArray jsonArray = JSON.parseArray(s);
        JSONObject o = (JSONObject)jsonArray.get(0);
        Double p = (Double)o.get("p");
        return p;
    }


}
