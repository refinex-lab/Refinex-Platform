package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrDrsInterfaceDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据资源接口 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrDrsInterfaceMapper extends BaseMapper<ScrDrsInterfaceDo> {

    /**
     * 按企业统计数据资源接口数量
     */
    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM scr_drs_interface di",
            "JOIN scr_drs d ON d.id = di.drs_id",
            "WHERE d.owner_estab_id = #{estabId}",
            "  AND d.deleted = 0",
            "  AND di.deleted = 0",
            "  AND di.id IN",
            "  <foreach collection='interfaceIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "</script>"
    })
    long countByIdsAndEstabId(@Param("estabId") Long estabId,
                              @Param("interfaceIds") List<Long> interfaceIds);
}
