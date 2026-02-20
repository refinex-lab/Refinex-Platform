package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_menu")
public class ScrMenu extends BaseEntity {

    private Long estabId;

    private Long systemId;

    private Long parentId;

    private String menuCode;

    private String menuName;

    private Integer menuType;

    private String path;

    private String icon;

    private Integer isBuiltin;

    private Integer visible;

    private Integer isFrame;

    private Integer status;

    private Integer sort;
}
