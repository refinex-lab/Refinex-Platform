package cn.refinex.system.infrastructure.persistence.repository;

import cn.refinex.base.response.PageResponse;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Override
    public PageResponse<DrsEntity> listDrs(Integer status, Long ownerEstabId, Integer dataOwnerType, String keyword,
                                           int currentPage, int pageSize) {
        LambdaQueryWrapper<ScrDrsDo> query = Wrappers.lambdaQuery(ScrDrsDo.class)
                .eq(ScrDrsDo::getDeleted, 0)
                .orderByAsc(ScrDrsDo::getId);
        if (status != null) {
            query.eq(ScrDrsDo::getStatus, status);
        }
        if (ownerEstabId != null) {
            query.eq(ScrDrsDo::getOwnerEstabId, ownerEstabId);
        }
        if (dataOwnerType != null) {
            query.eq(ScrDrsDo::getDataOwnerType, dataOwnerType);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(ScrDrsDo::getDrsCode, trimmed).or().like(ScrDrsDo::getDrsName, trimmed));
        }
        Page<ScrDrsDo> page = new Page<>(currentPage, pageSize);
        Page<ScrDrsDo> rowsPage = scrDrsMapper.selectPage(page, query);
        List<DrsEntity> result = new ArrayList<>();
        for (ScrDrsDo row : rowsPage.getRecords()) {
            result.add(drsDoConverter.toEntity(row));
        }
        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    @Override
    public DrsEntity findDrsById(Long drsId) {
        ScrDrsDo row = scrDrsMapper.selectById(drsId);
        return row == null ? null : drsDoConverter.toEntity(row);
    }

    @Override
    public long countDrsCode(Long ownerEstabId, String drsCode, Long excludeDrsId) {
        LambdaQueryWrapper<ScrDrsDo> query = Wrappers.lambdaQuery(ScrDrsDo.class)
                .eq(ScrDrsDo::getOwnerEstabId, ownerEstabId)
                .eq(ScrDrsDo::getDrsCode, drsCode)
                .eq(ScrDrsDo::getDeleted, 0);
        if (excludeDrsId != null) {
            query.ne(ScrDrsDo::getId, excludeDrsId);
        }
        Long count = scrDrsMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    @Override
    public DrsEntity insertDrs(DrsEntity drs) {
        ScrDrsDo row = drsDoConverter.toDo(drs);
        scrDrsMapper.insert(row);
        return drsDoConverter.toEntity(row);
    }

    @Override
    public void updateDrs(DrsEntity drs) {
        ScrDrsDo row = drsDoConverter.toDo(drs);
        scrDrsMapper.updateById(row);
    }

    @Override
    public void deleteDrsById(Long drsId) {
        scrDrsMapper.deleteById(drsId);
    }

    @Override
    public long countDrsInterfaces(Long drsId) {
        Long count = scrDrsInterfaceMapper.selectCount(
                Wrappers.lambdaQuery(ScrDrsInterfaceDo.class)
                        .eq(ScrDrsInterfaceDo::getDrsId, drsId)
                        .eq(ScrDrsInterfaceDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    @Override
    public PageResponse<DrsInterfaceEntity> listDrsInterfaces(Long drsId, Integer status, String keyword,
                                                              int currentPage, int pageSize) {
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
        Page<ScrDrsInterfaceDo> page = new Page<>(currentPage, pageSize);
        Page<ScrDrsInterfaceDo> rowsPage = scrDrsInterfaceMapper.selectPage(page, query);
        List<DrsInterfaceEntity> result = new ArrayList<>();
        for (ScrDrsInterfaceDo row : rowsPage.getRecords()) {
            result.add(drsInterfaceDoConverter.toEntity(row));
        }
        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    @Override
    public DrsInterfaceEntity findDrsInterfaceById(Long interfaceId) {
        ScrDrsInterfaceDo row = scrDrsInterfaceMapper.selectById(interfaceId);
        return row == null ? null : drsInterfaceDoConverter.toEntity(row);
    }

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

    @Override
    public DrsInterfaceEntity insertDrsInterface(DrsInterfaceEntity drsInterface) {
        ScrDrsInterfaceDo row = drsInterfaceDoConverter.toDo(drsInterface);
        scrDrsInterfaceMapper.insert(row);
        return drsInterfaceDoConverter.toEntity(row);
    }

    @Override
    public void updateDrsInterface(DrsInterfaceEntity drsInterface) {
        ScrDrsInterfaceDo row = drsInterfaceDoConverter.toDo(drsInterface);
        scrDrsInterfaceMapper.updateById(row);
    }

    @Override
    public void deleteDrsInterfaceById(Long interfaceId) {
        scrDrsInterfaceMapper.deleteById(interfaceId);
    }
}
