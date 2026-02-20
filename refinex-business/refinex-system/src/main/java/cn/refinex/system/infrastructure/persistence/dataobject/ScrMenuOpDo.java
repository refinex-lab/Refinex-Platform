package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单操作 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_menu_op")
public class ScrMenuOpDo extends BaseEntity {

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 操作编码
     */
    private String opCode;

    /**
     * 操作名称
     */
    private String opName;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序(升序)
     */
    private Integer sort;
}
