/**
 * 微信公众平台开发模式(JAVA) SDK
 * (c) 2012-2013 ____′↘夏悸 <wmails@126.cn>, MIT Licensed
 * http://www.jeasyuicn.com/wechat
 */
package com.gson.util;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;

import com.gson.bean.Attachment;

/**
 * https 请求 微信为https的请求
 * @author L.cm
 * @date 2013-10-9 下午2:40:19
 */
public class HttpKit {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String _GET  = "GET"; // GET
    private static final String _POST = "POST";// POST
    
    /**
     * 初始化http请求参数
     * @param url
     * @param method
     * @param headers
     * @return
     * @throws IOException
     */
    private static HttpURLConnection initHttp (String url, String method, Map<String, String> headers) throws IOException {
        URL _url = new URL(url);
        HttpURLConnection http = (HttpURLConnection) _url.openConnection();
        // 连接超时
        http.setConnectTimeout(25000);
        // 读取超时 --服务器响应比较慢，增大时间
        http.setReadTimeout(25000);
        http.setRequestMethod(method);
        http.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
        if (null != headers && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        http.setDoOutput(true);
        http.setDoInput(true);
        http.connect();
        return http;
    }
    
    /**
     * 初始化http请求参数
     * @param url
     * @param method
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws KeyManagementException
     */
    private static HttpsURLConnection initHttps (String url, String method, Map<String, String> headers) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        TrustManager[] tm = { new MyX509TrustManager() };  
        System.setProperty("https.protocols", "SSLv3");
        SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");  
        sslContext.init(null, tm, new java.security.SecureRandom());  
        // 从上述SSLContext对象中得到SSLSocketFactory对象  
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        URL _url = new URL(url);
        HttpsURLConnection http = (HttpsURLConnection) _url.openConnection();
        // 设置域名校验
        http.setHostnameVerifier(new HttpKit().new TrustAnyHostnameVerifier());
        // 连接超时
        http.setConnectTimeout(25000);
        // 读取超时 --服务器响应比较慢，增大时间
        http.setReadTimeout(25000);
        http.setRequestMethod(method);
        http.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
        if (null != headers && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        http.setSSLSocketFactory(ssf);
        http.setDoOutput(true);
        http.setDoInput(true);
        http.connect();
        return http;
    }

    /**
     * 
     * @description 
     * 功能描述: get 请求
     * @return       返回类型: 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, IOException {
        HttpURLConnection http = null;
        if (isHttps(url)) {
            http = initHttps(initParams(url, params), _GET, headers);
        } else {
            http = initHttp(initParams(url, params), _GET, headers);
        }
        InputStream in = http.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
        String valueString = null;
        StringBuffer bufferRes = new StringBuffer();
        while ((valueString = read.readLine()) != null){
            bufferRes.append(valueString);
        }
        in.close();
        if (http != null) {
            http.disconnect();// 关闭连接
        }
        return bufferRes.toString();
    }
    
    /**
     * 
     * @description 
     * 功能描述: get 请求
     * @return       返回类型: 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    public static String get(String url) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, IOException {
        return get(url, null);
    }
    
    /**
     * 
     * @description 
     * 功能描述: get 请求
     * @return       返回类型: 
     * @throws IOException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     * @throws UnsupportedEncodingException 
     */
    public static String get(String url, Map<String, String> params) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, IOException {
        return get(url, params, null);
    }
    
    /**
     * @description 
     * 功能描述: POST 请求
     * @return       返回类型: 
     * @throws IOException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    public static String post(String url, String params) throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        HttpURLConnection http = null;
        if (isHttps(url)) {
            http = initHttps(url, _POST, null);
        } else {
            http = initHttp(url, _POST, null);
        }
        OutputStream out = http.getOutputStream();
        out.write(params.getBytes(DEFAULT_CHARSET));
        out.flush();
        out.close();

        InputStream in = http.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
        String valueString = null;
        StringBuffer bufferRes = new StringBuffer();
        while ((valueString = read.readLine()) != null){
            bufferRes.append(valueString);
        }
        in.close();
        if (http != null) {
            http.disconnect();// 关闭连接
        }
        return bufferRes.toString();
    }
    
    /**
     * 上传媒体文件
     * @param url
     * @param params
     * @param file
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws KeyManagementException
     */
    public static String upload(String url,File file) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        String BOUNDARY = "----WebKitFormBoundaryiDGnV9zdZA1eM1yL"; // 定义数据分隔线  
        StringBuffer bufferRes = null;
        URL urlGet = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlGet.openConnection();
        conn.setDoOutput(true);  
        conn.setDoInput(true);  
        conn.setUseCaches(false);  
        conn.setRequestMethod("POST");  
        conn.setRequestProperty("connection", "Keep-Alive");  
        conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36");  
        conn.setRequestProperty("Charsert", "UTF-8");   
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);  
          
        OutputStream out = new DataOutputStream(conn.getOutputStream());  
        byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线  
        StringBuilder sb = new StringBuilder();    
        sb.append("--");    
        sb.append(BOUNDARY);    
        sb.append("\r\n");    
        sb.append("Content-Disposition: form-data;name=\"media\";filename=\""+ file.getName() + "\"\r\n");    
        sb.append("Content-Type:application/octet-stream\r\n\r\n");    
        byte[] data = sb.toString().getBytes();  
        out.write(data);  
        DataInputStream fs = new DataInputStream(new FileInputStream(file));  
        int bytes = 0;  
        byte[] bufferOut = new byte[1024];  
        while ((bytes = fs.read(bufferOut)) != -1) {  
            out.write(bufferOut, 0, bytes);  
        }  
        out.write("\r\n".getBytes()); //多个文件时，二个文件之间加入这个  
        fs.close();  
        out.write(end_data);  
        out.flush();    
        out.close();   

        // 定义BufferedReader输入流来读取URL的响应  
        InputStream in = conn.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
        String valueString = null;
        bufferRes = new StringBuffer();
        while ((valueString = read.readLine()) != null){
            bufferRes.append(valueString);
        }
        in.close();
        if (conn != null) {
            // 关闭连接
            conn.disconnect();
        }
        return bufferRes.toString();
    }
    
    /**
     * 下载资源
     * @param url
     * @return
     * @throws IOException
     */
    public static Attachment download(String url) throws IOException{
        Attachment att = new Attachment();
         HttpURLConnection conn = initHttp(url, _GET, null);
         if(conn.getContentType().equalsIgnoreCase("text/plain")){
            // 定义BufferedReader输入流来读取URL的响应  
             InputStream in = conn.getInputStream();
             BufferedReader read = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
             String valueString = null;
             StringBuffer bufferRes = new StringBuffer();
             while ((valueString = read.readLine()) != null){
                 bufferRes.append(valueString);
             }
             in.close();
             att.setError(bufferRes.toString());
         }else{
             BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());  
             String ds = conn.getHeaderField("Content-disposition");
             String fullName = ds.substring(ds.indexOf("filename=\"")+10,ds.length()-1);
             String relName = fullName.substring(0, fullName.lastIndexOf("."));
             String suffix = fullName.substring(relName.length()+1);

             att.setFullName(fullName);
             att.setFileName(relName);
             att.setSuffix(suffix);
             att.setContentLength(conn.getHeaderField("Content-Length"));
             att.setContentType(conn.getHeaderField("Content-Type"));

             att.setFileStream(bis);
         }
         return att;
    }

    /**
     * 功能描述: 构造请求参数
     * @return       返回类型: 
     * @throws UnsupportedEncodingException 
     */
    public static String initParams(String url, Map<String, String> params) throws UnsupportedEncodingException {
        if (null == params || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        if (url.indexOf("?") == -1) {
            sb.append("?");
        }
        sb.append(map2Url(params));
        return sb.toString();
    }
    
    /**
     * map构造url
     * @return       返回类型: 
     * @throws UnsupportedEncodingException 
     */
    public static String map2Url(Map<String, String> paramToMap) throws UnsupportedEncodingException {
        if (null == paramToMap || paramToMap.isEmpty()) {
            return null;
        }
        StringBuffer url = new StringBuffer();
        boolean isfist = true;
        for (Entry<String, String> entry : paramToMap.entrySet()) {
            if (isfist) {
                isfist = false;
            } else {
                url.append("&");
            }
            url.append(entry.getKey()).append("=");
            String value = entry.getValue();
            if (StringUtils.isNotEmpty(value)) {
                    url.append(URLEncoder.encode(value, DEFAULT_CHARSET));
            }
        }
        return url.toString();
    }
    
    /**
     * 检测是否https
     * @param url
     */
    private static boolean isHttps (String url) {
        return url.startsWith("https");
    }
    
    /**
     * https 域名校验
     * @param url
     * @param params
     * @return
     */
    public class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;// 直接返回true
        }
    }
    
    public static void main(String[] args) {
        String fname = "dsasdas.mp4";
        String s = fname.substring(0, fname.lastIndexOf("."));
        String f = fname.substring(s.length()+1);
        System.out.println(f);
    }
}

/**
 * 证书管理
 */
class MyX509TrustManager implements X509TrustManager {

    public X509Certificate[] getAcceptedIssuers() {
        return null;  
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }
}