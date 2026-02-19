package cn.refinex.system.infrastructure.persistence.repository;

import cn.refinex.system.domain.model.entity.DrsEntity;
import cn.refinex.system.domain.model.entity.DrsInterfaceEntity;
import cn.refinex.system.domain.repository.DataResourceRepository;
import cn.refinex.system.infrastructure.converter.DrsDoConverter;
import cn.refinex.system.infrastructure.converter.DrsInterfaceDoConverter;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrDrsDo;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrDrsInterfaceDo;
import cn.refinex.system.infrastructure.persistence.mapper.ScrDrsInterfaceMapper;
import cn.refinex.system.infrastructure.persistence.mapper.ScrDrsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据资源仓储实现
 *
 * @author refinex
 */
@Repository
@RequiredArgsConstructor
public class DataResourceRepositoryImpl implements DataResourceRepository {

    private final ScrDrsMapper scrDrsMapper;
    private final ScrDrsInterfaceMapper scrDrsInterfaceMapper;
    private final DrsDoConverter drsDoConverter;
    private final DrsInterfaceDoConverter drsInterfaceDoConverter;

    /**
     * 查询数据资源列表
     *
     * @param systemId     系统ID
     * @param status       状态
     * @param drsType      资源类型
     * @param ownerEstabId 所属组织ID
     * @param keyword      关键字
     * @return 数据资源列表
     */
    @Override
    public List<DrsEntity> listDrs(Long systemId, Integer status, Integer drsType, Long ownerEstabId, String keyword) {
        LambdaQueryWrapper<ScrDrsDo> query = Wrappers.lambdaQuery(ScrDrsDo.class)
                .eq(ScrDrsDo::getDeleted, 0)
                .orderByAsc(ScrDrsDo::getId);
        if (systemId != null) {
            query.eq(ScrDrsDo::getSystemId, systemId);
        }
        if (status != null) {
            query.eq(ScrDrsDo::getStatus, status);
        }
        if (drsType != null) {
            query.eq(ScrDrsDo::getDrsType, drsType);
        }
        if (ownerEstabId != null) {
            query.eq(ScrDrsDo::getOwnerEstabId, ownerEstabId);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(ScrDrsDo::getDrsCode, trimmed).or().like(ScrDrsDo::getDrsName, trimmed));
        }
        List<ScrDrsDo> rows = scrDrsMapper.selectList(query);
        List<DrsEntity> result = new ArrayList<>();
        for (ScrDrsDo row : rows) {
            result.add(drsDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据数据资源ID查询数据资源
     *
     * @param drsId 数据资源ID
     * @return 数据资源
     */
    @Override
    public DrsEntity findDrsById(Long drsId) {
        ScrDrsDo row = scrDrsMapper.selectById(drsId);
        return row == null ? null : drsDoConverter.toEntity(row);
    }

    /**
     * 根据系统ID和数据资源编码统计数据资源数量
     *
     * @param systemId     系统ID
     * @param drsCode      数据资源编码
     * @param excludeDrsId 排除的数据资源ID
     * @return 数据资源数量
     */
    @Override
    public long countDrsCode(Long systemId, String drsCode, Long excludeDrsId) {
        LambdaQueryWrapper<ScrDrsDo> query = Wrappers.lambdaQuery(ScrDrsDo.class)
                .eq(ScrDrsDo::getSystemId, systemId)
                .eq(ScrDrsDo::getDrsCode, drsCode)
                .eq(ScrDrsDo::getDeleted, 0);
        if (excludeDrsId != null) {
            query.ne(ScrDrsDo::getId, excludeDrsId);
        }
        Long count = scrDrsMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入数据资源
     *
     * @param drs 数据资源
     * @return 数据资源
     */
    @Override
    public DrsEntity insertDrs(DrsEntity drs) {
        ScrDrsDo row = drsDoConverter.toDo(drs);
        scrDrsMapper.insert(row);
        return drsDoConverter.toEntity(row);
    }

    /**
     * 更新数据资源
     *
     * @param drs 数据资源
     */
    @Override
    public void updateDrs(DrsEntity drs) {
        ScrDrsDo row = drsDoConverter.toDo(drs);
        scrDrsMapper.updateById(row);
    }

    /**
     * 删除数据资源（逻辑删除）
     *
     * @param drsId 数据资源ID
     */
    @Override
    public void deleteDrsById(Long drsId) {
        scrDrsMapper.deleteById(drsId);
    }

    /**
     * 根据数据资源ID统计接口数量
     *
     * @param drsId 数据资源ID
     * @return 接口数量
     */
    @Override
    public long countDrsInterfaces(Long drsId) {
        Long count = scrDrsInterfaceMapper.selectCount(
                Wrappers.lambdaQuery(ScrDrsInterfaceDo.class)
                        .eq(ScrDrsInterfaceDo::getDrsId, drsId)
                        .eq(ScrDrsInterfaceDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 查询数据资源接口列表
     *
     * @param drsId   数据资源ID
     * @param status  状态
     * @param keyword 关键字
     * @return 数据资源接口列表
     */
    @Override
    public List<DrsInterfaceEntity> listDrsInterfaces(Long drsId, Integer status, String keyword) {
        LambdaQueryWrapper<ScrDrsInterfaceDo> query = Wrappers.lambdaQuery(ScrDrsInterfaceDo.class)
                .eq(ScrDrsInterfaceDo::getDrsId, drsId)
                .eq(ScrDrsInterfaceDo::getDeleted, 0)
                .orderByAsc(ScrDrsInterfaceDo::getSort, ScrDrsInterfaceDo::getId);
        if (status != null) {
            query.eq(ScrDrsInterfaceDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(ScrDrsInterfaceDo::getInterfaceCode, trimmed)
                    .or().like(ScrDrsInterfaceDo::getInterfaceName, trimmed));
        }
        List<ScrDrsInterfaceDo> rows = scrDrsInterfaceMapper.selectList(query);
        List<DrsInterfaceEntity> result = new ArrayList<>();
        for (ScrDrsInterfaceDo row : rows) {
            result.add(drsInterfaceDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据接口ID查询数据资源接口
     *
     * @param interfaceId 接口ID
     * @return 数据资源接口
     */
    @Override
    public DrsInterfaceEntity findDrsInterfaceById(Long interfaceId) {
        ScrDrsInterfaceDo row = scrDrsInterfaceMapper.selectById(interfaceId);
        return row == null ? null : drsInterfaceDoConverter.toEntity(row);
    }

    /**
     * 根据数据资源ID和接口编码统计数据资源接口数量
     *
     * @param drsId              数据资源ID
     * @param interfaceCode      接口编码
     * @param excludeInterfaceId 排除的接口ID
     * @return 数据资源接口数量
     */
    @Override
    public long countDrsInterfaceCode(Long drsId, String interfaceCode, Long excludeInterfaceId) {
        LambdaQueryWrapper<ScrDrsInterfaceDo> query = Wrappers.lambdaQuery(ScrDrsInterfaceDo.class)
                .eq(ScrDrsInterfaceDo::getDrsId, drsId)
                .eq(ScrDrsInterfaceDo::getInterfaceCode, interfaceCode)
                .eq(ScrDrsInterfaceDo::getDeleted, 0);
        if (excludeInterfaceId != null) {
            query.ne(ScrDrsInterfaceDo::getId, excludeInterfaceId);
        }
        Long count = scrDrsInterfaceMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入数据资源接口
     *
     * @param drsInterface 数据资源接口
     * @return 数据资源接口
     */
    @Override
    public DrsInterfaceEntity insertDrsInterface(DrsInterfaceEntity drsInterface) {
        ScrDrsInterfaceDo row = drsInterfaceDoConverter.toDo(drsInterface);
        scrDrsInterfaceMapper.insert(row);
        return drsInterfaceDoConverter.toEntity(row);
    }

    /**
     * 更新数据资源接口
     *
     * @param drsInterface 数据资源接口
     */
    @Override
    public void updateDrsInterface(DrsInterfaceEntity drsInterface) {
        ScrDrsInterfaceDo row = drsInterfaceDoConverter.toDo(drsInterface);
        scrDrsInterfaceMapper.updateById(row);
    }

    /**
     * 删除数据资源接口（逻辑删除）
     *
     * @param interfaceId 接口ID
     */
    @Override
    public void deleteDrsInterfaceById(Long interfaceId) {
        scrDrsInterfaceMapper.deleteById(interfaceId);
    }
}
