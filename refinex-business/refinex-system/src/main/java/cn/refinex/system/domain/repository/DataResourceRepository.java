package cn.refinex.system.domain.repository;

import cn.refinex.base.response.PageResponse;
import cn.refinex.system.domain.model.entity.DrsEntity;
import cn.refinex.system.domain.model.entity.DrsInterfaceEntity;

/**
 * 数据资源仓储
 *
 * @author refinex
 */
public interface DataResourceRepository {

    /**
     * 查询数据资源列表
     */
    PageResponse<DrsEntity> listDrs(Integer status, Long ownerEstabId, Integer dataOwnerType, String keyword,
                                    int currentPage, int pageSize);

    /**
     * 查询数据资源
     */
    DrsEntity findDrsById(Long drsId);

    /**
     * 统计数据资源编码
     */
    long countDrsCode(Long ownerEstabId, String drsCode, Long excludeDrsId);

    /**
     * 插入数据资源
     */
    DrsEntity insertDrs(DrsEntity drs);

    /**
     * 更新数据资源
     */
    void updateDrs(DrsEntity drs);

    /**
     * 删除数据资源
     */
    void deleteDrsById(Long drsId);

    /**
     * 统计数据资源接口数量
     */
    long countDrsInterfaces(Long drsId);

    /**
     * 查询数据资源接口列表
     */
    PageResponse<DrsInterfaceEntity> listDrsInterfaces(Long drsId, Integer status, String keyword,
                                                       int currentPage, int pageSize);

    /**
     * 查询数据资源接口
     */
    DrsInterfaceEntity findDrsInterfaceById(Long interfaceId);

    /**
     * 统计数据资源接口编码
     */
    long countDrsInterfaceCode(Long drsId, String interfaceCode, Long excludeInterfaceId);

    /**
     * 插入数据资源接口
     */
    DrsInterfaceEntity insertDrsInterface(DrsInterfaceEntity drsInterface);

    /**
     * 更新数据资源接口
     */
    void updateDrsInterface(DrsInterfaceEntity drsInterface);

    /**
     * 删除数据资源接口
     */
    void deleteDrsInterfaceById(Long interfaceId);
}
