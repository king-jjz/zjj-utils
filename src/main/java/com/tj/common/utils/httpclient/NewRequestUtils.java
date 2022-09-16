package com.tj.common.utils.httpclient;

import com.tj.entity.enums.ValidEnum;
import com.tj.entity.po.http.HttpResult;
import com.tj.entity.po.httplog.HttpLogOutPO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网络编程(需日志记录版)
 */

@Component
public class NewRequestUtils {

    /**
     *
     * 使用HttpClient发送请求、接收响应很简单，一般需要如下几步即可。
     *
     * 1. 创建HttpClient对象。
     *
     * 2. 创建请求方法的实例，并指定请求URL。如果需要发送GET请求，创建HttpGet对象；如果需要发送POST请求，创建HttpPost对象。
     *
     * 3. 如果需要发送请求参数，可调用HttpGet、HttpPost共同的setParams(HetpParams
     * params)方法来添加请求参数；对于HttpPost对象而言，也可调用setEntity(HttpEntity entity)方法来设置请求参数。
     *
     * 4. 调用HttpClient对象的execute(HttpUriRequest request)发送请求，该方法返回一个HttpResponse。
     *
     * 5. 调用HttpResponse的getAllHeaders()、getHeaders(String
     * name)等方法可获取服务器的响应头；调用HttpResponse的getEntity()方法可获取HttpEntity对象，该对象包装了服务器的响应内容。程序可通过该对象获取服务器的响应内容。
     *
     * 6. 释放连接。无论执行方法是否成功，都必须释放连接
     */

    /**
     * post请求(模拟form表单提交)
     *
     * @param url
     * @return
     */

    public static HttpResult doPostWithForm(String url, Map param) {
        HttpPost post = new HttpPost(url);
        return postHttpClientWithForm(post, param);
    }

    public static HttpResult postHttpClientWithForm(HttpPost post, Map<String, String> params) {
        //封装HttpResult实体类
        HttpResult httpResult = new HttpResult();
        try {
            ArrayList<BasicNameValuePair> pairList = new ArrayList<>();
            params.forEach((k, v) -> pairList.add(new BasicNameValuePair(k, v)));

            CloseableHttpClient client = HttpClients.createDefault();
            if (params != null && params.size() > 0) {
                post.setEntity(new UrlEncodedFormEntity(pairList, "UTF-8"));
            }

            HttpLogOutPO httpLogOutPO = getReqHttpLogOut(post); //初始化请求数据，并且封装对象
            //设置超时时间
            post.setConfig(setTimeout());
            //发送请求
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            //设置参数
            httpResult.setStatusCode(response.getStatusLine().getStatusCode() + "");
            httpResult.setContent(EntityUtils.toString(entity, "UTF-8"));
            setRespHttpLogOut(httpResult, httpLogOutPO);
            httpResult.setHttpLogOutPO(httpLogOutPO);

            if (client != null) {
                client.close();
            }
        } catch (HttpHostConnectException e) {
            //证明url不存在 伪404
            //HttpHostConnectException
            sethttpResultInfo(httpResult,"EXC-404", e.getMessage(), new HttpLogOutPO(post.getRequestLine().getUri(), "POST", "", params.toString(), "EXC-404", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (InterruptedIOException e) {
            //证明url 连接超时 伪504
            //SocketTimeoutException
            sethttpResultInfo(httpResult,"EXC-504", e.getMessage(), new HttpLogOutPO(post.getRequestLine().getUri(), "POST", "", params.toString(), "EXC-504", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (IOException e) {
            //其他
            sethttpResultInfo(httpResult,"EXC-ERROR", e.getMessage(), new HttpLogOutPO(post.getRequestLine().getUri(), "POST", "", params.toString(), "EXC-ERROR", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        }

        return httpResult;
    }

    /**
     * get请求
     *
     * @param uri
     * @return
     */
    public static HttpResult doGet(String uri) {

        CloseableHttpClient client = HttpClients.createDefault();
        // 创建POST请求方法
        HttpGet get = new HttpGet(uri);

        HttpLogOutPO httpLogOutPO = getReqHttpLogOut(get); //初始化请求数据，并且封装对象

        //设置超时时间
        get.setConfig(setTimeout());
        //封装HttpResult实体类
        HttpResult httpResult = new HttpResult();

        try {
            //发送请求
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            httpResult.setStatusCode(response.getStatusLine().getStatusCode() + "");
            httpResult.setContent(EntityUtils.toString(entity, "UTF-8"));
            setRespHttpLogOut(httpResult, httpLogOutPO);
            httpResult.setHttpLogOutPO(httpLogOutPO);
            if (client != null) {
                client.close();
            }
        } catch (HttpHostConnectException e) {
            //证明url不存在 伪404
            //HttpHostConnectException
            sethttpResultInfo(httpResult,"EXC-404", e.getMessage(), new HttpLogOutPO(uri, "GET", "", "", "EXC-404", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (InterruptedIOException e) {
            //证明url 连接超时 伪504
            //SocketTimeoutException
            sethttpResultInfo(httpResult,"EXC-504", e.getMessage(), new HttpLogOutPO(uri, "GET", "", "", "EXC-504", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (IOException e) {
            //其他
            sethttpResultInfo(httpResult,"EXC-ERROR", e.getMessage(), new HttpLogOutPO(uri, "GET", "", "", "EXC-ERROR", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        }
        return httpResult;
    }

    /**
     * PUT请求
     *
     * @param url
     * @return
     */
    public static HttpResult doPut(String url, String param) {
        HttpPut put = new HttpPut(url);
        return putHttpClient(put, param);
    }

    public static HttpResult doPut(String url, String param, Map<String, String> map) {
        HttpPut put = new HttpPut(url);
        map.forEach((k, v) -> put.setHeader(k, v));
        return putHttpClient(put, param);
    }

    public static HttpResult putHttpClient(HttpPut put, String param) {
        //封装HttpResult实体类
        HttpResult httpResult = new HttpResult();
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            // 创建POST请求方法
            put.setHeader("Accept", "application/json");
            put.setHeader("Content-Type", "application/json");
            put.setEntity(new StringEntity(param, "UTF-8"));
            HttpLogOutPO httpLogOutPO = getReqHttpLogOut(put); //初始化请求数据，并且封装对象
            //设置超时时间
            put.setConfig(setTimeout());
            //发送请求
            HttpResponse response = client.execute(put);
            HttpEntity entity = response.getEntity();
            //设置参数
            httpResult.setStatusCode(response.getStatusLine().getStatusCode() + "");
            httpResult.setContent(EntityUtils.toString(entity, "UTF-8"));
            setRespHttpLogOut(httpResult, httpLogOutPO);
            httpResult.setHttpLogOutPO(httpLogOutPO);

            if (client != null) {
                client.close();
            }
        }catch (HttpHostConnectException e) {
            //证明url不存在 伪404
            //HttpHostConnectException
            sethttpResultInfo(httpResult,"EXC-404", e.getMessage(), new HttpLogOutPO(put.getRequestLine().getUri(), "PUT", "", param, "EXC-404", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (InterruptedIOException e) {
            //证明url 连接超时 伪504
            //SocketTimeoutException
            sethttpResultInfo(httpResult,"EXC-504", e.getMessage(), new HttpLogOutPO(put.getRequestLine().getUri(), "PUT", "", param, "EXC-504", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (IOException e) {
            //其他
            sethttpResultInfo(httpResult,"EXC-ERROR", e.getMessage(), new HttpLogOutPO(put.getRequestLine().getUri(), "PUT", "", param, "EXC-ERROR", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        }
        return httpResult;
    }


    /**
     * post请求
     *
     * @param url
     * @return
     */
    public static HttpResult doPost(String url, String param) {
        HttpPost post = new HttpPost(url);
        return postHttpClient(post, param);
    }

    /**
     * post请求（可添加header）
     *
     * @param url
     * @param param
     * @param headerMaps
     * @return
     */
    public static HttpResult doPost(String url, String param, Map<String, String> headerMaps) {
        HttpPost post = new HttpPost(url);
        headerMaps.forEach((k, v) -> post.setHeader(k, v));
        return postHttpClient(post, param);
    }


    public static HttpResult postHttpClient(HttpPost post, String param) {
        //封装HttpResult实体类
        HttpResult httpResult = new HttpResult();

        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/json");

        CloseableHttpClient client = HttpClients.createDefault();
        if (!StringUtils.isEmpty(param) && !"null".equalsIgnoreCase(param)) {
            post.setEntity(new StringEntity(param, "UTF-8"));
        }
        try {
            HttpLogOutPO httpLogOutPO = getReqHttpLogOut(post); //初始化请求数据，并且封装对象
            //设置超时时间
            post.setConfig(setTimeout());
            //发送请求
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            httpResult.setStatusCode(response.getStatusLine().getStatusCode() + "");
            httpResult.setContent(EntityUtils.toString(entity, "UTF-8"));
            setRespHttpLogOut(httpResult, httpLogOutPO);
            httpResult.setHttpLogOutPO(httpLogOutPO);

            if (client != null) {
                client.close();
            }
        }catch (HttpHostConnectException e) {
            //证明url不存在 伪404
            //HttpHostConnectException
            sethttpResultInfo(httpResult, "EXC-404", e.getMessage(), new HttpLogOutPO(post.getRequestLine().getUri(), "POST", "", param, "EXC-404", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (InterruptedIOException e) {
            //证明url 连接超时 伪504
            //SocketTimeoutException
            sethttpResultInfo(httpResult,"EXC-504", e.getMessage(), new HttpLogOutPO(post.getRequestLine().getUri(), "POST", "", param, "EXC-504", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        } catch (IOException e) {
            //其他
            sethttpResultInfo(httpResult,"EXC-ERROR", e.getMessage(), new HttpLogOutPO(post.getRequestLine().getUri(), "POST", "", param, "EXC-ERROR", e.getMessage(), -1l, LocalDateTime.now(), LocalDateTime.now(), "VALID"));
        }

        return httpResult;
    }


    /**
     * @param httpBase
     * @return HttpLogOutPO
     * @time 2018/9/12 0012
     * @description 初始化请求数据，并且封装对象，针对POST/PUT方法
     */
    public static HttpLogOutPO getReqHttpLogOut(HttpEntityEnclosingRequestBase httpBase) throws IOException {

        HttpLogOutPO httpLogOutPO = new HttpLogOutPO();
        httpLogOutPO.setReqUrl(httpBase.getRequestLine().getUri());     //url
        httpLogOutPO.setReqMethod(httpBase.getMethod());                 //请求方法
        if (httpBase.getEntity() != null) {
            httpLogOutPO.setReqParams(URLDecoder.decode(new BufferedReader(new InputStreamReader(httpBase.getEntity().getContent(), "utf-8")).lines().collect(Collectors.joining(System.lineSeparator())), "UTF-8"));
            httpLogOutPO.setReqType(httpBase.getEntity().getContentType().getValue());      // 请求类型
        }

        httpLogOutPO.setRespTime(System.currentTimeMillis());//记录请求时间
        LocalDateTime createTime = LocalDateTime.now();
        httpLogOutPO.setCreateTime(createTime);        // 创建时间
        httpLogOutPO.setUpdateTime(createTime);        // 更新时间
        httpLogOutPO.setStat(ValidEnum.VALID.name());  // 状态

        return httpLogOutPO;
    }

    /**
     * @param httpBase
     * @return HttpLogOutPO
     * @time 2018/9/12 0012
     * @description 初始化请求数据，并且封装对象，针对GET方法
     */
    public static HttpLogOutPO getReqHttpLogOut(HttpRequestBase httpBase) {

        HttpLogOutPO httpLogOutPO = new HttpLogOutPO();
        httpLogOutPO.setReqUrl(httpBase.getRequestLine().getUri());     //url
        httpLogOutPO.setReqMethod(httpBase.getMethod());                //请求方法
        httpLogOutPO.setRespTime(System.currentTimeMillis());           //记录请求时间
        LocalDateTime createTime = LocalDateTime.now();
        httpLogOutPO.setCreateTime(createTime);        // 创建时间
        httpLogOutPO.setUpdateTime(createTime);        // 更新时间
        httpLogOutPO.setStat(ValidEnum.VALID.name());  // 状态

        return httpLogOutPO;
    }

    /**
     * @param httpResult
     * @param httpLogOutPO
     * @return void
     * @time 2018/9/12 0012
     * @description 设置响应体内容, 并且封装对象
     */
    public static void setRespHttpLogOut(HttpResult httpResult, HttpLogOutPO httpLogOutPO) {
        httpLogOutPO.setRespResult(httpResult.getContent());
        httpLogOutPO.setRespCode(String.valueOf(httpResult.getStatusCode()));
        httpLogOutPO.setRespTime(System.currentTimeMillis() - httpLogOutPO.getRespTime());
    }

    /**
     * 设置超时时间
     *
     * @return
     */
    public static RequestConfig setTimeout() {
        return RequestConfig.custom().setConnectTimeout(15000000).setConnectionRequestTimeout(15000000).setSocketTimeout(15000000).build();
    }

    /**
     * 设置httpResult信息
     */
    public static void sethttpResultInfo(HttpResult httpResult,String statusCode, String content, HttpLogOutPO httpLogOutPO) {
        httpResult.setStatusCode(statusCode);
        httpResult.setContent(content);
        httpResult.setHttpLogOutPO(httpLogOutPO);
    }

    public static HttpResult doGetWithBasic(String url, String userName, String password) {
        HttpGet get = new HttpGet(url);
        return getBasic(get, userName, password);
    }

    public static CloseableHttpClient getHttpClient(String name, String password){
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(name, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        return  HttpClients.custom().setDefaultCredentialsProvider(provider).build();
    }

    private static HttpResult getBasic(HttpGet get, String userName, String password) {
        CloseableHttpClient httpClient = getHttpClient(userName, password);
        HttpLogOutPO httpLogOutPO = null; //初始化请求数据，并且封装对象
        HttpResult httpResult = new HttpResult();
        String url = "";
        try {
            url = get.getRequestLine().getUri();
            httpLogOutPO = getReqHttpLogOut(get);
            CloseableHttpResponse response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String code = String.valueOf(response.getStatusLine().getStatusCode());
            httpResult.setStatusCode(code);
            httpResult.setContent(EntityUtils.toString(entity, "UTF-8"));
            //封装HttpResult实体类
            setRespHttpLogOut(httpResult, httpLogOutPO);
            httpResult.setHttpLogOutPO(httpLogOutPO);
            if (httpClient != null) {
                httpClient.close();
            }
        }  catch (InterruptedIOException e) {
            setExceptionHttpResult(httpResult, "EXC-504", url, e.getMessage(), "GET");
        }  catch (HttpHostConnectException e) {
            setExceptionHttpResult(httpResult, "EXC-404", url, e.getMessage(), "GET");
        } catch (IOException e) {
            setExceptionHttpResult(httpResult, "EXC-ERROR", url, e.getMessage(), "GET");
        }
        return httpResult;
    }

    private static void setExceptionHttpResult(HttpResult httpResult, String code, String url, String message, String method) {
        LocalDateTime now = LocalDateTime.now();
        httpResult.setStatusCode(code);
        httpResult.setContent(message);
        HttpLogOutPO httpLogOutPO = new HttpLogOutPO(url, method, "", "", code, message, System.currentTimeMillis(), now, now, ValidEnum.VALID.name());
        httpResult.setHttpLogOutPO(httpLogOutPO);
    }
}
