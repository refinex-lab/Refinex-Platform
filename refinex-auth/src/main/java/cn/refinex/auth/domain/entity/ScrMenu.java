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

    private Long systemId;

    private Long parentId;

    private String menuCode;

    private String menuName;

    private Integer menuType;

    private String path;

    private String component;

    private String permissionKey;

    private String icon;

    private Integer visible;

    private Integer isFrame;

    private Integer isCache;

    private Integer status;

    private Integer sort;
}
