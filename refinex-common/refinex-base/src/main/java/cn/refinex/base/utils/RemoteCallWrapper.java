package cn.refinex.base.utils;

import cn.refinex.base.exception.RemoteCallException;
import cn.refinex.base.exception.code.BizErrorCode;
import cn.refinex.base.exception.code.RepoErrorCode;
import com.alibaba.fastjson2.JSON;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 远程调用包装工具类
 * <p>
 * 为第三方服务调用提供统一的切面能力，包含：
 * 1. 自动记录请求/响应日志及耗时（Monitor）。
 * 2. 统一异常捕获与转换（Exception Translation）。
 * 3. 响应结果的标准化校验（Validation）。
 *
 * @author refinex
 */
@Slf4j
@UtilityClass
public class RemoteCallWrapper {

    /**
     * 判定为成功的状态码集合（可根据业务扩展）
     */
    private static final Set<String> SUCCESS_CODES = Set.of("SUCCESS", "DUPLICATE", "DUPLICATED_REQUEST");

    /**
     * 判定为成功的布尔方法名集合
     */
    private static final Set<String> SUCCESS_METHOD_NAMES = Set.of("isSuccess", "isSucceeded", "getSuccess");

    /**
     * 获取状态码的方法名
     */
    private static final String CODE_METHOD_NAME = "getResponseCode";

    /**
     * 反射方法缓存，Key 为类类型，Value 为对应的方法（避免频繁反射）
     */
    private static final Map<Class<?>, Method> SUCCESS_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> CODE_METHOD_CACHE = new ConcurrentHashMap<>();

    // 空对象标记，用于缓存 “未找到方法” 的情况，防止缓存穿透
    private static final Method NO_METHOD_FOUND;

    static {
        try {
            NO_METHOD_FOUND = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to initialize NO_METHOD_FOUND method cache sentinel", e);
        }
    }

    /**
     * 执行远程调用（默认开启响应校验）
     *
     * @param function 远程调用逻辑 lambda
     * @param request  请求参数
     * @param <T>      请求类型
     * @param <R>      响应类型
     * @return 响应结果
     */
    public static <T, R> R call(Function<T, R> function, T request) {
        return call(function, request, request.getClass().getSimpleName(), true, false);
    }

    /**
     * 执行远程调用（指定是否校验）
     *
     * @param function      远程调用逻辑 lambda
     * @param request       请求参数
     * @param checkResponse 是否校验响应结果
     * @param <T>           请求类型
     * @param <R>           响应类型
     * @return 响应结果
     */
    public static <T, R> R call(Function<T, R> function, T request, boolean checkResponse) {
        return call(function, request, request.getClass().getSimpleName(), checkResponse, false);
    }

    /**
     * 执行远程调用（指定请求名称）
     *
     * @param function    远程调用逻辑 lambda
     * @param request     请求参数
     * @param requestName 请求名称
     * @param <T>         请求类型
     * @param <R>         响应类型
     * @return 响应结果
     */
    public static <T, R> R call(Function<T, R> function, T request, String requestName) {
        return call(function, request, requestName, true, false);
    }

    /**
     * 执行远程调用（全参数控制）
     *
     * @param function          远程调用函数
     * @param request           请求参数
     * @param requestName       请求名称（用于日志标识）
     * @param checkResponse     是否校验 Boolean 类型成功标识 (如 isSuccess)
     * @param checkResponseCode 是否校验 String 类型错误码 (如 getResponseCode)
     * @param <T>               入参类型
     * @param <R>               出参类型
     * @return 远程调用结果
     */
    public static <T, R> R call(Function<T, R> function, T request, String requestName, boolean checkResponse, boolean checkResponseCode) {
        long start = System.currentTimeMillis();
        R response = null;
        Throwable exception = null;

        try {
            // 执行实际的远程调用
            response = function.apply(request);

            // 基础非空校验
            if (response == null) {
                throw new RemoteCallException("Remote call response is null", BizErrorCode.REMOTE_CALL_RESPONSE_IS_NULL);
            }

            // 1. 校验布尔返回值 (isSuccess / success)
            if (checkResponse && !isResponseSuccess(response)) {
                logInvalidResponse(requestName, request, response, "Boolean check failed");
                throw new RemoteCallException(JSON.toJSONString(response), BizErrorCode.REMOTE_CALL_RESPONSE_IS_FAILED);
            }

            // 2. 校验状态码 (code)
            if (checkResponseCode && !isResponseCodeValid(response)) {
                logInvalidResponse(requestName, request, response, "Code check failed");
                throw new RemoteCallException(JSON.toJSONString(response), BizErrorCode.REMOTE_CALL_RESPONSE_IS_FAILED);
            }

            return response;

        } catch (RemoteCallException e) {
            exception = e;
            throw e;
        } catch (Throwable e) {
            exception = e;
            log.error("[RemoteCall] Exception occurred. Method={}, Request={}", requestName, JSON.toJSONString(request), e);
            // 包装为业务受检异常抛出，保留原始堆栈
            throw new RemoteCallException(e.getMessage(), e, RepoErrorCode.UNKNOWN_ERROR);
        } finally {
            // 统一记录日志
            long cost = System.currentTimeMillis() - start;
            if (log.isInfoEnabled()) {
                String responseStr = (exception != null) ? "EXCEPTION: " + exception.getMessage() : JSON.toJSONString(response);
                log.info("## [RemoteCall] Method={}, Cost={}ms, Request={}, Response={}", requestName, cost, JSON.toJSONString(request), responseStr);
            }
        }
    }

    /**
     * 校验响应对象中的成功标识方法（支持缓存）
     *
     * @param response 响应对象
     * @return 是否成功
     */
    private static boolean isResponseSuccess(Object response) {
        Method method = getCachedMethod(response.getClass(), SUCCESS_METHOD_CACHE, SUCCESS_METHOD_NAMES);

        // 如果对象没有相关方法，默认认为不需要校验，返回 true
        if (method == NO_METHOD_FOUND) {
            return true;
        }

        try {
            Object result = method.invoke(response);
            return result instanceof Boolean bool && bool;
        } catch (Exception e) {
            log.warn("Failed to invoke success method on response: {}", response.getClass().getName(), e);
            return false;
        }
    }

    /**
     * 校验响应对象中的状态码方法（支持缓存）
     *
     * @param response 响应对象
     * @return 是否有效
     */
    private static boolean isResponseCodeValid(Object response) {
        Method method = getCachedMethod(response.getClass(), CODE_METHOD_CACHE, Set.of(CODE_METHOD_NAME));

        if (method == NO_METHOD_FOUND) {
            return true;
        }

        try {
            Object code = method.invoke(response);
            return code != null && SUCCESS_CODES.contains(code.toString());
        } catch (Exception e) {
            log.warn("Failed to invoke code method on response: {}", response.getClass().getName(), e);
            return false;
        }
    }

    /**
     * 从缓存或反射获取方法
     *
     * @param clazz      类型
     * @param cache      缓存
     * @param candidates 方法名候选项
     * @return 方法
     */
    private static Method getCachedMethod(Class<?> clazz, Map<Class<?>, Method> cache, Set<String> candidates) {
        return cache.computeIfAbsent(clazz, key -> {
            for (String name : candidates) {
                try {
                    return key.getMethod(name);
                } catch (NoSuchMethodException e) {
                    // ignore and try next
                }
            }
            return NO_METHOD_FOUND;
        });
    }

    /**
     * 记录无效响应
     *
     * @param requestName 请求名称
     * @param request     请求参数
     * @param response    响应结果
     * @param reason      无效原因
     */
    private static void logInvalidResponse(String requestName, Object request, Object response, String reason) {
        log.error("[RemoteCall] Invalid Response ({}). Method={}, Request={}, Response={}",
                reason, requestName, JSON.toJSONString(request), JSON.toJSONString(response));
    }
}
