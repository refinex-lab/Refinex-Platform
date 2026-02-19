package cn.refinex.system.domain.repository;

import cn.refinex.system.domain.model.entity.ValueEntity;
import cn.refinex.system.domain.model.entity.ValueSetEntity;

import java.util.List;

/**
 * 值集仓储
 *
 * @author refinex
 */
public interface ValueSetRepository {

    /**
     * 查询值集列表
     *
     * @param status  状态
     * @param keyword 关键字
     * @return 值集列表
     */
    List<ValueSetEntity> listValueSets(Integer status, String keyword);

    /**
     * 根据值集ID查询值集
     *
     * @param valueSetId 值集ID
     * @return 值集
     */
    ValueSetEntity findValueSetById(Long valueSetId);

    /**
     * 根据值集编码查询值集
     *
     * @param setCode 值集编码
     * @return 值集
     */
    ValueSetEntity findValueSetByCode(String setCode);

    /**
     * 根据值集编码统计值集数量
     *
     * @param setCode           值集编码
     * @param excludeValueSetId 排除的值集ID
     * @return 值集数量
     */
    long countValueSetCode(String setCode, Long excludeValueSetId);

    /**
     * 插入值集
     *
     * @param valueSet 值集
     * @return 值集
     */
    ValueSetEntity insertValueSet(ValueSetEntity valueSet);

    /**
     * 更新值集
     *
     * @param valueSet 值集
     */
    void updateValueSet(ValueSetEntity valueSet);

    /**
     * 删除值集（逻辑删除）
     *
     * @param valueSetId 值集ID
     */
    void deleteValueSetById(Long valueSetId);

    /**
     * 根据值集编码统计值数量
     *
     * @param setCode 值集编码
     * @return 值数量
     */
    long countValuesBySetCode(String setCode);

    /**
     * 查询值集明细列表
     *
     * @param setCode 值集编码
     * @param status  状态
     * @param keyword 关键字
     * @return 值集明细列表
     */
    List<ValueEntity> listValues(String setCode, Integer status, String keyword);

    /**
     * 根据值ID查询值集明细
     *
     * @param valueId 值ID
     * @return 值集明细
     */
    ValueEntity findValueById(Long valueId);

    /**
     * 根据值集编码和值编码统计值数量
     *
     * @param setCode        值集编码
     * @param valueCode      值编码
     * @param excludeValueId 排除的值ID
     * @return 值数量
     */
    long countValueCode(String setCode, String valueCode, Long excludeValueId);

    /**
     * 清空指定值集的默认值标记（排除某条记录）
     *
     * @param setCode        值集编码
     * @param excludeValueId 排除的值ID
     */
    void clearDefaultValue(String setCode, Long excludeValueId);

    /**
     * 插入值集明细
     *
     * @param value 值集明细
     * @return 值集明细
     */
    ValueEntity insertValue(ValueEntity value);

    /**
     * 更新值集明细
     *
     * @param value 值集明细
     */
    void updateValue(ValueEntity value);

    /**
     * 删除值集明细（逻辑删除）
     *
     * @param valueId 值ID
     */
    void deleteValueById(Long valueId);
}
