package cn.refinex.base.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RestClient HTTP 工具类 (Apache HttpClient 5 封装)
 * <p>
 * 基于 Spring 6+ {@link RestClient} 与 Apache HttpClient 5 集成。
 * 包含连接池管理、超时控制及状态码拦截。
 *
 * @author refinex
 */
@Slf4j
@UtilityClass
public class RestClientUtils {

    /**
     * 全局单例 RestClient
     */
    private static final RestClient REST_CLIENT;

    static {
        // 1. 配置连接池管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // 最大连接数 (整个连接池)
        connectionManager.setMaxTotal(200);
        // 每个路由(域名)的最大并发数
        connectionManager.setDefaultMaxPerRoute(50);

        // 配置连接相关的超时 (HttpClient 5 区分了 Socket 和 Connect 配置)
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(3000))    // 连接握手超时
                .setSocketTimeout(Timeout.ofMilliseconds(5000))     // 数据读取超时(Socket)
                .setTimeToLive(10, TimeUnit.MINUTES)      // 连接最大存活时间
                .build());

        // 2. 配置请求级参数
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(2000)) // 从连接池获取连接的等待超时
                .build();

        // 3. 构建 HttpClient 5 实例
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries() // 建议生产环境关闭自动重试，由上层业务控制重试逻辑
                .build();

        // 4. 构建 Spring 适配工厂
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 5. 初始化 RestClient
        REST_CLIENT = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, "Refinex-RestClient/1.0")
                .build();
    }

    /**
     * 发送 GET 请求
     *
     * @param host         域名
     * @param path         路径
     * @param headersMap   请求头
     * @param queries      查询参数
     * @param responseType 响应类型
     * @param <T>          泛型
     * @return 响应实体
     */
    public static <T> ResponseEntity<T> doGet(String host, String path, Map<String, String> headersMap, Map<String, String> queries, Class<T> responseType) {
        URI uri = buildUri(host, path, queries);

        return REST_CLIENT.get()
                .uri(uri)
                .headers(httpHeaders -> fillHeaders(httpHeaders, headersMap))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) ->
                        log.warn("HTTP Client Error: {} {}, Status: {}", request.getMethod(), request.getURI(), response.getStatusCode()))
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) ->
                        log.error("HTTP Server Error: {} {}, Status: {}", request.getMethod(), request.getURI(), response.getStatusCode()))
                .toEntity(responseType);
    }

    /**
     * 发送 POST 请求 (返回 String)
     *
     * @param host         域名
     * @param path         路径
     * @param headersMap   请求头
     * @param queries      查询参数
     * @param bodies       请求体
     * @return 响应结果
     */
    public static ResponseEntity<String> doPost(String host, String path, Map<String, String> headersMap, Map<String, String> queries, Object bodies) {
        return doPost(host, path, headersMap, queries, bodies, String.class);
    }

    /**
     * 发送 POST 请求（泛型支持）
     *
     * @param host         域名
     * @param path         路径
     * @param headersMap   请求头
     * @param queries      查询参数
     * @param bodies       请求体
     * @param responseType 响应类型 Class
     * @param <T>          响应泛型
     * @return 包含 T 类型响应体的 ResponseEntity
     */
    public static <T> ResponseEntity<T> doPost(String host, String path, Map<String, String> headersMap, Map<String, String> queries, Object bodies, Class<T> responseType) {

        URI uri = buildUri(host, path, queries);

        return REST_CLIENT.post()
                .uri(uri)
                .headers(httpHeaders -> fillHeaders(httpHeaders, headersMap))
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodies)
                .retrieve()
                // 建议：生产环境如果遇到4xx/5xx，通常需要抛出自定义异常以便上层捕获
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) ->
                    log.error("HTTP Client Error: {} {}, Status: {}", request.getMethod(), request.getURI(), response.getStatusCode())
                )
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) ->
                    log.error("HTTP Server Error: {} {}, Status: {}", request.getMethod(), request.getURI(), response.getStatusCode())
                )
                .toEntity(responseType);
    }

    /**
     * 使用 UriComponentsBuilder 安全构建 URI
     *
     * @param host         域名
     * @param path         路径
     * @param queries      查询参数
     * @return URI
     */
    private static URI buildUri(String host, String path, Map<String, String> queries) {
        // Spring 6+ / Boot 3+ 变更：使用 fromUriString 替代 fromHttpUrl
        // 注意：host 必须包含协议前缀，例如 "http://api.example.com"
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(host);

        if (StringUtils.hasText(path)) {
            builder.path(path);
        }

        if (!CollectionUtils.isEmpty(queries)) {
            queries.forEach(builder::queryParam);
        }

        // build().toUri() 自动处理了编码问题
        return builder.encode(StandardCharsets.UTF_8).build().toUri();
    }

    /**
     * 填充请求头
     *
     * @param httpHeaders  请求头
     * @param headersMap   头信息
     */
    private static void fillHeaders(HttpHeaders httpHeaders, Map<String, String> headersMap) {
        if (!CollectionUtils.isEmpty(headersMap)) {
            headersMap.forEach(httpHeaders::add);
        }
    }
}
