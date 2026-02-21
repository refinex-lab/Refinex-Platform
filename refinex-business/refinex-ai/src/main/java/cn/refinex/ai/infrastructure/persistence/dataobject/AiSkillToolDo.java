package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技能-工具关联 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_skill_tool")
public class AiSkillToolDo extends BaseEntity {

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 工具ID
     */
    private Long toolId;

    /**
     * 排序(升序)
     */
    private Integer sort;
}
