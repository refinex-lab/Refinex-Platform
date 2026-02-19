package cn.refinex.system.infrastructure.persistence.repository;

import cn.refinex.system.domain.model.entity.ValueEntity;
import cn.refinex.system.domain.model.entity.ValueSetEntity;
import cn.refinex.system.domain.repository.ValueSetRepository;
import cn.refinex.system.infrastructure.converter.ValueDoConverter;
import cn.refinex.system.infrastructure.converter.ValueSetDoConverter;
import cn.refinex.system.infrastructure.persistence.dataobject.AppValueDo;
import cn.refinex.system.infrastructure.persistence.dataobject.AppValueSetDo;
import cn.refinex.system.infrastructure.persistence.mapper.AppValueMapper;
import cn.refinex.system.infrastructure.persistence.mapper.AppValueSetMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 值集仓储实现
 *
 * @author refinex
 */
@Repository
@RequiredArgsConstructor
public class ValueSetRepositoryImpl implements ValueSetRepository {

    private final AppValueSetMapper appValueSetMapper;
    private final AppValueMapper appValueMapper;
    private final ValueSetDoConverter valueSetDoConverter;
    private final ValueDoConverter valueDoConverter;

    /**
     * 查询值集列表
     *
     * @param status  状态
     * @param keyword 关键字
     * @return 值集列表
     */
    @Override
    public List<ValueSetEntity> listValueSets(Integer status, String keyword) {
        LambdaQueryWrapper<AppValueSetDo> query = Wrappers.lambdaQuery(AppValueSetDo.class)
                .eq(AppValueSetDo::getDeleted, 0)
                .orderByAsc(AppValueSetDo::getSort, AppValueSetDo::getId);
        if (status != null) {
            query.eq(AppValueSetDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(AppValueSetDo::getSetCode, trimmed).or().like(AppValueSetDo::getSetName, trimmed));
        }
        List<AppValueSetDo> rows = appValueSetMapper.selectList(query);
        List<ValueSetEntity> result = new ArrayList<>();
        for (AppValueSetDo row : rows) {
            result.add(valueSetDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据值集ID查询值集
     *
     * @param valueSetId 值集ID
     * @return 值集
     */
    @Override
    public ValueSetEntity findValueSetById(Long valueSetId) {
        AppValueSetDo row = appValueSetMapper.selectById(valueSetId);
        return row == null ? null : valueSetDoConverter.toEntity(row);
    }

    /**
     * 根据值集编码查询值集
     *
     * @param setCode 值集编码
     * @return 值集
     */
    @Override
    public ValueSetEntity findValueSetByCode(String setCode) {
        AppValueSetDo row = appValueSetMapper.selectOne(
                Wrappers.lambdaQuery(AppValueSetDo.class)
                        .eq(AppValueSetDo::getSetCode, setCode)
                        .eq(AppValueSetDo::getDeleted, 0)
                        .last("LIMIT 1")
        );
        return row == null ? null : valueSetDoConverter.toEntity(row);
    }

    /**
     * 根据值集编码统计值集数量
     *
     * @param setCode           值集编码
     * @param excludeValueSetId 排除的值集ID
     * @return 值集数量
     */
    @Override
    public long countValueSetCode(String setCode, Long excludeValueSetId) {
        LambdaQueryWrapper<AppValueSetDo> query = Wrappers.lambdaQuery(AppValueSetDo.class)
                .eq(AppValueSetDo::getSetCode, setCode)
                .eq(AppValueSetDo::getDeleted, 0);
        if (excludeValueSetId != null) {
            query.ne(AppValueSetDo::getId, excludeValueSetId);
        }
        Long count = appValueSetMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入值集
     *
     * @param valueSet 值集
     * @return 值集
     */
    @Override
    public ValueSetEntity insertValueSet(ValueSetEntity valueSet) {
        AppValueSetDo row = valueSetDoConverter.toDo(valueSet);
        appValueSetMapper.insert(row);
        return valueSetDoConverter.toEntity(row);
    }

    /**
     * 更新值集
     *
     * @param valueSet 值集
     */
    @Override
    public void updateValueSet(ValueSetEntity valueSet) {
        AppValueSetDo row = valueSetDoConverter.toDo(valueSet);
        appValueSetMapper.updateById(row);
    }

    /**
     * 删除值集（逻辑删除）
     *
     * @param valueSetId 值集ID
     */
    @Override
    public void deleteValueSetById(Long valueSetId) {
        appValueSetMapper.deleteById(valueSetId);
    }

    /**
     * 根据值集编码统计值数量
     *
     * @param setCode 值集编码
     * @return 值数量
     */
    @Override
    public long countValuesBySetCode(String setCode) {
        Long count = appValueMapper.selectCount(
                Wrappers.lambdaQuery(AppValueDo.class)
                        .eq(AppValueDo::getSetCode, setCode)
                        .eq(AppValueDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 查询值集明细列表
     *
     * @param setCode 值集编码
     * @param status  状态
     * @param keyword 关键字
     * @return 值集明细列表
     */
    @Override
    public List<ValueEntity> listValues(String setCode, Integer status, String keyword) {
        LambdaQueryWrapper<AppValueDo> query = Wrappers.lambdaQuery(AppValueDo.class)
                .eq(AppValueDo::getSetCode, setCode)
                .eq(AppValueDo::getDeleted, 0)
                .orderByAsc(AppValueDo::getSort, AppValueDo::getId);
        if (status != null) {
            query.eq(AppValueDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(AppValueDo::getValueCode, trimmed).or().like(AppValueDo::getValueName, trimmed));
        }
        List<AppValueDo> rows = appValueMapper.selectList(query);
        List<ValueEntity> result = new ArrayList<>();
        for (AppValueDo row : rows) {
            result.add(valueDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据值ID查询值集明细
     *
     * @param valueId 值ID
     * @return 值集明细
     */
    @Override
    public ValueEntity findValueById(Long valueId) {
        AppValueDo row = appValueMapper.selectById(valueId);
        return row == null ? null : valueDoConverter.toEntity(row);
    }

    /**
     * 根据值集编码和值编码统计值数量
     *
     * @param setCode        值集编码
     * @param valueCode      值编码
     * @param excludeValueId 排除的值ID
     * @return 值数量
     */
    @Override
    public long countValueCode(String setCode, String valueCode, Long excludeValueId) {
        LambdaQueryWrapper<AppValueDo> query = Wrappers.lambdaQuery(AppValueDo.class)
                .eq(AppValueDo::getSetCode, setCode)
                .eq(AppValueDo::getValueCode, valueCode)
                .eq(AppValueDo::getDeleted, 0);
        if (excludeValueId != null) {
            query.ne(AppValueDo::getId, excludeValueId);
        }
        Long count = appValueMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 清空指定值集的默认值标记（排除某条记录）
     *
     * @param setCode        值集编码
     * @param excludeValueId 排除的值ID
     */
    @Override
    public void clearDefaultValue(String setCode, Long excludeValueId) {
        appValueMapper.clearDefaultBySetCode(setCode, excludeValueId);
    }

    /**
     * 插入值集明细
     *
     * @param value 值集明细
     * @return 值集明细
     */
    @Override
    public ValueEntity insertValue(ValueEntity value) {
        AppValueDo row = valueDoConverter.toDo(value);
        appValueMapper.insert(row);
        return valueDoConverter.toEntity(row);
    }

    /**
     * 更新值集明细
     *
     * @param value 值集明细
     */
    @Override
    public void updateValue(ValueEntity value) {
        AppValueDo row = valueDoConverter.toDo(value);
        appValueMapper.updateById(row);
    }

    /**
     * 删除值集明细（逻辑删除）
     *
     * @param valueId 值ID
     */
    @Override
    public void deleteValueById(Long valueId) {
        appValueMapper.deleteById(valueId);
    }
}
