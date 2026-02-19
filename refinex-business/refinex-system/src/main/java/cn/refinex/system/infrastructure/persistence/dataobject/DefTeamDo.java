package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队/部门 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_team")
public class DefTeamDo extends BaseEntity {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 团队编码
     */
    private String teamCode;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 父团队ID
     */
    private Long parentId;

    /**
     * 负责人用户ID
     */
    private Long leaderUserId;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
