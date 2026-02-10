package cn.refinex.base.response;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部 REST 接口适配响应
 * <p>
 * 用于封装符合标准 REST 格式的外部调用结果。
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true) // 禁用继承自父类的 equals 和 hashCode 方法的生成
public class RestResponse extends BaseResponse {

    /**
     * 核心业务数据
     */
    private JSONObject data;

    /**
     * 错误明细信息
     */
    private JSONObject error;

    /**
     * 获取响应是否成功
     *
     * @return 响应是否成功
     */
    @Override
    public Boolean getSuccess() {
        return data != null;
    }

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    @Override
    public String getResponseMessage() {
        if (this.error != null) {
            return error.getString("message");
        }
        return super.getResponseMessage();
    }

    /**
     * 获取响应状态码
     *
     * @return 响应状态码
     */
    @Override
    public String getResponseCode() {
        if (this.error != null) {
            return error.getString("code");
        }
        return super.getResponseCode();
    }
}
