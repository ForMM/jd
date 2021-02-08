package com.tt.jdstar.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUrlConnectionClient {
	private static Logger logger = LoggerFactory.getLogger(HttpUrlConnectionClient.class);
	
	private HttpUrlConnectionClient() {}
	
	/**
	 * get请求
	 * @param httpurl
	 * @return
	 */
	public static String doGet(String httpurl) {
		HttpURLConnection connection = null;
		InputStream is = null;
		BufferedReader br =null;
		String result=null;
		try {
			URL url = new URL(httpurl);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			//连接服务器超时时间
			connection.setConnectTimeout(15000);
			//读取远程地址数据返回时间
			connection.setReadTimeout(60000);
			connection.connect();
			
			if(connection.getResponseCode() == 200) {
				is = connection.getInputStream();
				br = new BufferedReader(new InputStreamReader(is,"utf-8"));
				StringBuilder bf = new StringBuilder();
				String temp=null;
				while((temp=br.readLine())!=null){
					bf.append(temp);
					bf.append("\r\n");
				}
				result=bf.toString();
			}
			
		} catch (MalformedURLException e) {
			logger.error("doGet have MalformedURLException problem!",e);
		} catch (IOException e) {
			logger.error("doGet have IOException problem!",e);
		}finally{
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("inputStream close",e);
				}
			}
			if(br!=null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("br close",e);
				}
			}
			if(connection!=null) {
				connection.disconnect();
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param httpurl
	 * @param param
	 * @return
	 */
	public static String doPost(String httpurl,String param) {
		HttpURLConnection connection = null;
		InputStream is = null;
		OutputStream os = null;
		BufferedReader br =null;
		String result="";
		try {
			URL url = new URL(httpurl);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			//连接服务器超时时间
			connection.setConnectTimeout(15000);
			//读取远程地址数据返回时间
			connection.setReadTimeout(60000);
			// 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
			connection.connect();
			
            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            os.write(param.getBytes());
            if(connection.getResponseCode() == 200) {
				is = connection.getInputStream();
				br = new BufferedReader(new InputStreamReader(is,"utf-8"));
				StringBuilder bf = new StringBuilder();
				String temp=null;
				while((temp=br.readLine())!=null){
					bf.append(temp);
					bf.append("\r\n");
				}
				result=bf.toString();
			}
			
		}catch (Exception e) {
			logger.error("doPost have Exception problem!",e);
		}finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("inputStream close",e);
				}
			}
			if(br!=null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("br close",e);
				}
			}
			if(os!=null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error("os close",e);
				}
			}
			if(connection!=null) {
				connection.disconnect();
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		String url = "http://service-i7iwxtym-1255747603.gz.apigw.tencentcs.com/release/api/account_register.api";
		String doPost = HttpUrlConnectionClient.doPost(url, "");
		logger.info("result:{}",doPost);
		String doGet = HttpUrlConnectionClient.doGet("");
		logger.info("result:{}",doGet);
	}
	
	
}
