package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.AppValueDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 值集明细 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AppValueMapper extends BaseMapper<AppValueDo> {

    /**
     * 清空指定值集的默认值标记（排除某条记录）
     *
     * @param setCode   值集编码
     * @param excludeId 排除ID
     * @return 影响行数
     */
    @Update("""
            <script>
            UPDATE app_value
            SET is_default = 0
            WHERE set_code = #{setCode}
              AND deleted = 0
            <if test='excludeId != null'>
              AND id != #{excludeId}
            </if>
            </script>
            """)
    int clearDefaultBySetCode(@Param("setCode") String setCode, @Param("excludeId") Long excludeId);
}
