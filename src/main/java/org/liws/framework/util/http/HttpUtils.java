package org.liws.framework.util.http;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.liws.framework.util.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;

public class HttpUtils {

    /**
     * 通过httprequest 获取用户的请求ip
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_VIA");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)){
            //根据网卡取本机配置的IP
            InetAddress inet=null;
            try {
                inet = getLocalHostLANAddress();
            } catch (UnknownHostException | SocketException e) {
                // logger.error(e);
            }
            if(inet != null){
                ip= inet.getHostAddress();
            }

        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ip!=null && ip.contains(",")){
            String[] arr = ip.split(",");
            for(String e : arr){
                if(!"unknown".equalsIgnoreCase(e) && !StringUtils.isEmpty(e)){
                    ip = e;
                    break;
                }
            }
        }
        return ip;
    }

    private static InetAddress getLocalHostLANAddress() throws SocketException, UnknownHostException {

        InetAddress candidateAddress = null;
        // 遍历所有的网络接口
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
            NetworkInterface iface = ifaces.nextElement();
            // 在所有的接口下再遍历IP
            for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                InetAddress inetAddr = inetAddrs.nextElement();
                if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                    if (inetAddr.isSiteLocalAddress()) {
                        // 如果是site-local地址，就是它了
                        return inetAddr;
                    } else if (candidateAddress == null) {
                        // site-local类型的地址未被发现，先记录候选地址
                        candidateAddress = inetAddr;
                    }
                }
            }
        }
        if (candidateAddress != null) {
            return candidateAddress;
        }
        // 如果没有发现 non-loopback地址.只能用最次选的方案
        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
        return jdkSuppliedAddress;


    }
    
    /**
     * 获取服务器Host
     * @param requestUrl
     * @param indexOfValue
     * @return
     */
    public static String getRequestHostURL(String requestUrl, String indexOfValue) throws RuntimeException {
    	String url = null;
    	try {
            if(requestUrl != null && requestUrl.indexOf(indexOfValue)>0) {
            	url = requestUrl.substring(0, requestUrl.indexOf(indexOfValue));
            } else {
            	throw new RuntimeException("URL:" + requestUrl + "不存在" + indexOfValue + "路径！");
            }
     	} catch (Exception e) {
     		throw new RuntimeException(e.getMessage());
     	}
    	
        return url;
    }

    @FunctionalInterface
    public static interface CloseableHttpResponseCallback<T>{
        public T invoke(CloseableHttpResponse httpResponse)throws IOException ;
    }

    /**
     * 做http get请求，并使用回调处理结果
     * @param url
     * @param callback
     * @return
     * @throws IOException
     */
    public static <T> T doHttpGet(String url,CloseableHttpResponseCallback<T> callback){
        HttpGet httpGet = null;
        try(CloseableHttpClient httpClient = HttpClients.createDefault();) {
            httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json");// Content-Type:application/json
            httpGet.setHeader("accept", "*/*");
            httpGet.setHeader("connection", "Keep-Alive");
            httpGet.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            return callback.invoke(httpClient.execute(httpGet));
        } catch (IOException e) {
            // logger.error("get请求提交失败:" + url, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 做http get请求，返回字符串
     * @param url
     * @return
     * @throws IOException
     */
    public static String doHttpGet(String url){
        return doHttpGet(url,HttpUtils::getContentString);
    }
    /**
     * 做http post请求，并使用回调处理结果
     * @param url
     * @param callback
     * @return
     * @throws Exception
     */
    public static <T> T doHttpPost(String url, String json, Map<String, String> headerParams,CloseableHttpResponseCallback<T> callback){
        HttpPost httpPost = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()){

            httpPost = new HttpPost(url);
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

            // add headerParams处理
            if (headerParams != null && headerParams.size() > 0) {
                for(String he : headerParams.keySet()) {
                    httpPost.setHeader(he, headerParams.get(he));
                }
            }

            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

                httpPost.setHeader("Content-type", "application/json");
                StringEntity s = new StringEntity(json,"utf-8");
                s.setContentEncoding("utf-8");
                s.setContentType("application/json");//发送json数据需要设置contentType
                httpPost.setEntity(s);


            return callback.invoke(httpClient.execute(httpPost));
        } catch (Exception e) {
            // logger.error("POST请求提交失败:" + url, e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 做http post请求，并使用回调处理结果
     * @param url
     * @param params
     * @param params
     * @param callback
     * @return
     * @throws Exception
     */
    public static <T> T doHttpPost(String url, String type, Map<String, Object> params, Map<String, String> headerParams,CloseableHttpResponseCallback<T> callback){
        HttpPost httpPost = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            
            // add headerParams处理
			if (headerParams != null && headerParams.size() > 0) {
				for(String he : headerParams.keySet()) {
					httpPost.setHeader(he, headerParams.get(he));
				}
            }

            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            if("json".equalsIgnoreCase(type)){
                httpPost.setHeader("Content-type", "application/json");
                StringEntity s = new StringEntity(JSONUtil.toJson(params),"utf-8");
                s.setContentEncoding("utf-8");
                s.setContentType("application/json");//发送json数据需要设置contentType
                httpPost.setEntity(s);
 
            }else{
                httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
                    String name = iter.next();
                    String value = String.valueOf(params.get(name));
                    nvps.add(new BasicNameValuePair(name, value));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps,"utf-8"));
            }


            return callback.invoke(httpClient.execute(httpPost));
        } catch (Exception e) {
            // logger.error("POST请求提交失败:" + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                if(httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                // logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /**
	 * 做http post请求，返回字符串
	 * @param url 请求url
	 * @param params 请求体参数
	 * @param headerParams 请求头参数
	 * @return
	 * @throws IOException
	 */
	public static String doHttpPost(String url, Map<String, Object> params, Map<String, String> headerParams) {
		return doHttpPost(url, "json", params, headerParams, HttpUtils::getContentString);
	}

    /**
     * 做http post请求，返回字符串
     * @param url 请求url
     * @param json 请求体参数
     * @return
     * @throws IOException
     */
    public static String doHttpPost(String url, String json) {
        return doHttpPost(url, json,null, HttpUtils::getContentString);
    }

	/**
     * 做http post请求，返回字符串
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String doHttpPost(String url, Map<String, Object> params) {
		return doHttpPost(url, params, null);
	}

    /**
     * 做http post请求，返回字符串
     * @param url
     * @param type
     * @param params
     * @param headerParams
     * @return
     * @throws IOException
     */
    public static String doHttpPost(String url, String type, Map<String, Object> params, Map<String, String> headerParams) {
    	return doHttpPost(url, type, params, headerParams, HttpUtils::getContentString);
    }

    /**
     * 把CloseableHttpResponse转换为UTF-8字符集的字符串
     * @param httpResponse
     * @return
     * @throws IOException
     */
    public static String getContentString(CloseableHttpResponse httpResponse) throws IOException {
        return getContentString(httpResponse,Charset.forName("UTF-8"));
    }

    /**
     * 把CloseableHttpResponse转换为指定字符集的字符串
     * @param httpResponse
     * @param charset
     * @return
     * @throws IOException
     */
    public static String getContentString(CloseableHttpResponse httpResponse,Charset charset) throws IOException {
        if (httpResponse != null && httpResponse.getEntity() != null) {
            //start 读取整个页面内容
            try(InputStream is = httpResponse.getEntity().getContent()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(is, charset));
                StringBuilder buffer = new StringBuilder();
                String line ;
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }

                //end 读取整个页面内容
                return buffer.toString();
            }
        }else{
            return null;
        }
    }
}
