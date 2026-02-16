package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统定义
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_system")
public class ScrSystem extends BaseEntity {

    private String systemCode;

    private String systemName;

    private Integer systemType;

    private String baseUrl;

    private Integer status;

    private Integer sort;

    private String remark;
}
