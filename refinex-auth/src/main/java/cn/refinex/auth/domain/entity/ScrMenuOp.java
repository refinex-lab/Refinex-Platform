package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单操作定义
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_menu_op")
public class ScrMenuOp extends BaseEntity {

    private Long menuId;

    private String opCode;

    private String opName;

    private String httpMethod;

    private String pathPattern;

    private String permissionKey;

    private Integer status;

    private Integer sort;
}
