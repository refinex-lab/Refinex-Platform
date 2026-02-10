package cn.refinex.base.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基础请求对象
 * <p>
 * 所有业务请求参数类均应继承此类，确保序列化能力及扩展性。
 *
 * @author refinex
 */
@Data
public class BaseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
