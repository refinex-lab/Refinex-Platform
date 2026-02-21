package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库目录 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_folder")
public class KbFolderDo extends BaseEntity {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 父目录ID(0为根级)
     */
    private Long parentId;

    /**
     * 目录名称
     */
    private String folderName;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息
     */
    private String extJson;
}
