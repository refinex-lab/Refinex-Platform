package cn.refinex.base.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求工具类
 * <p>
 * 基于 Apache HttpClient 5.x 实现，封装了 Get、Post、Put、Delete 等常用方法。
 * 内置连接池管理，支持高并发场景。
 *
 * @author refinex
 */
@UtilityClass
public class HttpUtils {

    /**
     * 全局 HTTP 客户端（单例，线程安全）
     */
    private static final CloseableHttpClient HTTP_CLIENT;

    /**
     * 默认字符集
     */
    private static final String CHARSET_UTF8 = StandardCharsets.UTF_8.name();

    static {
        // 配置连接池
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // 最大连接数
        connectionManager.setMaxTotal(500);
        // 每个路由（域名）的最大并发数
        connectionManager.setDefaultMaxPerRoute(50);
        // 连接建立超时
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(5000))
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        // 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000)) // 从池中获取连接超时
                .setResponseTimeout(Timeout.ofMilliseconds(10000))         // 读取超时
                .build();

        HTTP_CLIENT = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * 执行 GET 请求
     *
     * @param host    域名，例如：<a href="http://www.example.com">...</a>
     * @param path    路径，例如：/api/v1/user
     * @param headers 请求头
     * @param queries 查询参数
     * @return 响应对象 (调用方需负责关闭资源或读取实体)
     * @throws IOException      请求异常
     * @throws URISyntaxException URI 构建异常
     */
    public static CloseableHttpResponse doGet(String host, String path, Map<String, String> headers, Map<String, String> queries) throws IOException, URISyntaxException {
        HttpGet request = new HttpGet(buildUri(host, path, queries));
        addHeaders(request, headers);
        return executeOpen(request);
    }

    /**
     * 执行 POST 表单请求
     *
     * @param host    域名
     * @param path    路径
     * @param headers 请求头
     * @param queries URL 查询参数
     * @param bodies  Form 表单参数
     * @return 响应对象
     * @throws IOException      请求异常
     * @throws URISyntaxException URI 构建异常
     */
    public static CloseableHttpResponse doPost(String host, String path, Map<String, String> headers, Map<String, String> queries, Map<String, String> bodies) throws IOException, URISyntaxException {
        HttpPost request = new HttpPost(buildUri(host, path, queries));
        addHeaders(request, headers);

        if (MapUtils.isNotEmpty(bodies)) {
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for (Map.Entry<String, String> entry : bodies.entrySet()) {
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            request.setEntity(new UrlEncodedFormEntity(nameValuePairList));
        }

        return executeOpen(request);
    }

    /**
     * 执行 POST String 请求 (如 JSON)
     *
     * @param host    域名
     * @param path    路径
     * @param headers 请求头
     * @param queries URL 查询参数
     * @param body    请求体字符串
     * @return 响应对象
     * @throws IOException      请求异常
     * @throws URISyntaxException URI 构建异常
     */
    public static CloseableHttpResponse doPost(String host, String path, Map<String, String> headers, Map<String, String> queries, String body) throws IOException, URISyntaxException {
        HttpPost request = new HttpPost(buildUri(host, path, queries));
        addHeaders(request, headers);

        if (StringUtils.isNotBlank(body)) {
            request.setEntity(new StringEntity(body, ContentType.parse(CHARSET_UTF8)));
        }

        return executeOpen(request);
    }

    /**
     * 执行 POST 二进制流请求
     *
     * @param host    域名
     * @param path    路径
     * @param headers 请求头
     * @param queries URL 查询参数
     * @param body    二进制数据
     * @return 响应对象
     * @throws IOException      请求异常
     * @throws URISyntaxException URI 构建异常
     */
    public static CloseableHttpResponse doPost(String host, String path, Map<String, String> headers, Map<String, String> queries, byte[] body) throws IOException, URISyntaxException {
        HttpPost request = new HttpPost(buildUri(host, path, queries));
        addHeaders(request, headers);

        if (body != null) {
            request.setEntity(new ByteArrayEntity(body, ContentType.DEFAULT_BINARY));
        }

        return executeOpen(request);
    }

    /**
     * 执行 PUT String 请求
     *
     * @param host    域名
     * @param path    路径
     * @param headers 请求头
     * @param queries URL 查询参数
     * @param body    请求体字符串
     * @return 响应对象
     * @throws IOException      请求异常
     * @throws URISyntaxException URI 构建异常
     */
    public static CloseableHttpResponse doPut(String host, String path, Map<String, String> headers, Map<String, String> queries, String body) throws IOException, URISyntaxException {
        HttpPut request = new HttpPut(buildUri(host, path, queries));
        addHeaders(request, headers);

        if (StringUtils.isNotBlank(body)) {
            request.setEntity(new StringEntity(body, ContentType.parse(CHARSET_UTF8)));
        }

        return executeOpen(request);
    }

    /**
     * 执行 PUT 二进制流请求
     *
     * @param host    域名
     * @param path    路径
     * @param headers 请求头
     * @param queries URL 查询参数
     * @param body    二进制数据
     * @return 响应对象
     * @throws IOException      请求异常
     * @throws URISyntaxException URI 构建异常
     */
    public static CloseableHttpResponse doPut(String host, String path, Map<String, String> headers, Map<String, String> queries, byte[] body) throws IOException, URISyntaxException {
        HttpPut request = new HttpPut(buildUri(host, path, queries));
        addHeaders(request, headers);

        if (body != null) {
            request.setEntity(new ByteArrayEntity(body, ContentType.DEFAULT_BINARY));
        }

        return executeOpen(request);
    }

    /**
     * 执行 DELETE 请求
     *
     * @param host    域名
     * @param path    路径
     * @param headers 请求头
     * @param queries URL 查询参数
     * @return 响应对象
     * @throws IOException      请求异常
     * @throws URISyntaxException URI 构建异常
     */
    public static CloseableHttpResponse doDelete(String host, String path, Map<String, String> headers, Map<String, String> queries) throws IOException, URISyntaxException {
        HttpDelete request = new HttpDelete(buildUri(host, path, queries));
        addHeaders(request, headers);
        return executeOpen(request);
    }

    /**
     * 构建 URI
     *
     * @param host    域名
     * @param path    路径
     * @param queries 查询参数
     * @return 构建后的 URI 字符串
     * @throws URISyntaxException URI 构建异常
     */
    private static String buildUri(String host, String path, Map<String, String> queries) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(host);

        if (StringUtils.isNotBlank(path)) {
            builder.setPath(path);
        }

        if (queries != null) {
            for (Map.Entry<String, String> query : queries.entrySet()) {
                if (StringUtils.isNotBlank(query.getKey())) {
                    builder.addParameter(query.getKey(), query.getValue());
                }
            }
        }
        return builder.build().toString();
    }

    /**
     * 执行请求并返回可关闭响应（避免使用已弃用的 execute 方法）
     *
     * @param request 请求对象
     * @return 响应对象
     * @throws IOException 请求异常
     */
    private static CloseableHttpResponse executeOpen(HttpUriRequestBase request) throws IOException, URISyntaxException {
        HttpHost target = HttpHost.create(request.getUri());
        return (CloseableHttpResponse) HTTP_CLIENT.executeOpen(target, request, null);
    }

    /**
     * 添加请求头
     *
     * @param request 请求对象
     * @param headers 头信息 Map
     */
    private static void addHeaders(HttpUriRequestBase request, Map<String, String> headers) {
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
    }
}
