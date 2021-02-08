package com.tt.jdstar.http;

import com.alibaba.fastjson.JSONObject;
import com.tt.jdstar.io.FileUtil;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {
	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	public static CookieStore cookieStore;

	private HttpClientUtil() {}
	
	public static String doGet(JSONObject headers, String httpUrl) {
		String result = "";
//		HttpClient client = null;
		HttpResponse httpResponse = null;
		cookieStore = new BasicCookieStore();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore).build();

//		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(1000).setConnectTimeout(1000)
//				.setSocketTimeout(1000).build();
//		client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpGet httpGet = new HttpGet(httpUrl);

		if (headers != null) {
			Iterator<String> iterator = headers.keySet().iterator();
			while (iterator.hasNext()) {
				String headerName = iterator.next();
				httpGet.setHeader(headerName, headers.get(headerName).toString());
			}
		}


		try {
			httpResponse = httpClient.execute(httpGet);
			result = EntityUtils.toString(httpResponse.getEntity());
			List<Cookie> cookies = cookieStore.getCookies();
			for (int i = 0; i < cookies.size(); i++) {
				System.out.println("Local cookie: " + cookies.get(i));
			}
			} catch (ClientProtocolException e) {
			logger.error("HttpClientUtil doGet exception", e);
		} catch (IOException e) {
			logger.error("HttpClientUtil doGet IOException", e);
		}finally {
			httpGet.releaseConnection();
		}

		return result;
	}

	/**
	 * 通过http请求下载文件
	 * @param headers
	 * @param httpUrl
	 * @return
	 */
	public static boolean doGetDownloadFile(JSONObject headers, String httpUrl) {
		HttpResponse httpResponse = null;
		cookieStore = new BasicCookieStore();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore).build();
		HttpGet httpGet = new HttpGet(httpUrl);
		if (headers != null) {
			Iterator<String> iterator = headers.keySet().iterator();
			while (iterator.hasNext()) {
				String headerName = iterator.next();
				httpGet.setHeader(headerName, headers.get(headerName).toString());
			}
		}

		try {
			httpResponse = httpClient.execute(httpGet);
			byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());

			FileUtil.BytesToFile(bytes,"/Users/hayashika/program-kk/pro/","qrCode.png");

			List<Cookie> cookies = cookieStore.getCookies();
			for (int i = 0; i < cookies.size(); i++) {
				logger.info("Local cookie: " + cookies.get(i));
			}
		} catch (ClientProtocolException e) {
			logger.error("HttpClientUtil doGet exception", e);
			return false;
		} catch (IOException e) {
			logger.error("HttpClientUtil doGet IOException", e);
			return false;
		}finally {
			httpGet.releaseConnection();
		}

		return true;
	}

	/**
	 * 不带参数
	 * @param httpUrl 地址
	 * @return
	 */
	public static String sendHttpPost(String httpUrl) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		return doPost(httpPost);
	}
	
	/**
	 * 带上参数(参数格式:key1=value1&key2=value2) 
	 * @param httpUrl
	 * @param params
	 * @return
	 */
	public static String sendHttpPost(String httpUrl,String params) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		StringEntity stringEntity = new StringEntity(params, "UTF-8");
        stringEntity.setContentType("application/x-www-form-urlencoded");  
        httpPost.setEntity(stringEntity);  
        return doPost(httpPost);
	}
	
	/**
	 * 带参数（参数格式Map集合）
	 * @param httpUrl
	 * @param params
	 * @return
	 */
	public static String sendHttpPost(String httpUrl,Map<String,String> params,Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		//创建参数队列
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		for(String key: params.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
		}
		for(String key: headers.keySet()) {
			httpPost.setHeader(key, params.get(key));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("sendHttpPost UnsupportedEncodingException",e);
		}
		return doPost(httpPost);
	}
	
	/**
	 * 带参数（参数格式为Map集合并带上文件）
	 * @param httpUrl
	 * @param params
	 * @param fileLists
	 * @return
	 */
	public static String sendHttpPost(String httpUrl,Map<String,String> params,List<File> fileLists) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		//创建参数队列
		MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create(); 
		for(String key: params.keySet()) {
			meBuilder.addPart(key, new StringBody(params.get(key), ContentType.TEXT_PLAIN));
		}
		for(File file:fileLists) {
			FileBody fileBody = new FileBody(file);  
            meBuilder.addPart("files", fileBody);  
		}
		HttpEntity httpEntity = meBuilder.build();
		httpPost.setEntity(httpEntity);
		return doPost(httpPost);
	}
	
	public static String doPost(HttpPost httpPost) {
		String result="";
		HttpClient client = null;
		HttpResponse httpResponse = null;
		RequestConfig requestConfig = getRequestConfig();
		client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		try {
			httpResponse = client.execute(httpPost);
			logger.info(String.format("响应状态:%s",httpResponse));
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(httpResponse.getEntity());
			}else {
				StringBuffer errorMsg = new StringBuffer();
				errorMsg.append("http status:");
				errorMsg.append(httpResponse.getStatusLine().getStatusCode());
				errorMsg.append(httpResponse.getStatusLine().getReasonPhrase());
				errorMsg.append(",header:");
				Header[] allHeaders = httpResponse.getAllHeaders();
				for(Header header:allHeaders) {
					errorMsg.append(header.getName()).append(":").append(header.getValue());
				}
				logger.info("httpResponse error:{}",errorMsg);
			}
		} catch (ClientProtocolException e) {
			logger.error("HttpClientUtil doGet exception", e);
		} catch (IOException e) {
			logger.error("HttpClientUtil doGet IOException", e);
		}finally {
			httpPost.releaseConnection();
		}
		return result;
	}
	
	private static RequestConfig getRequestConfig() {
		return RequestConfig.custom().setConnectionRequestTimeout(1000).setConnectTimeout(1000)
				.setSocketTimeout(1000).build();
	}
	
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		String doGet = HttpClientUtil.doGet(null,"https://www.baidu.com/");
		logger.info("doGet result:{}",doGet);
	}
	
	
}
