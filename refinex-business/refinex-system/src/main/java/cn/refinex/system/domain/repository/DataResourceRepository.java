package cn.refinex.system.domain.repository;

import cn.refinex.system.domain.model.entity.DrsEntity;
import cn.refinex.system.domain.model.entity.DrsInterfaceEntity;

import java.util.List;

/**
 * 数据资源仓储
 *
 * @author refinex
 */
public interface DataResourceRepository {

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
    List<DrsEntity> listDrs(Long systemId, Integer status, Integer drsType, Long ownerEstabId, String keyword);

    /**
     * 根据数据资源ID查询数据资源
     *
     * @param drsId 数据资源ID
     * @return 数据资源
     */
    DrsEntity findDrsById(Long drsId);

    /**
     * 根据系统ID和数据资源编码统计数据资源数量
     *
     * @param systemId     系统ID
     * @param drsCode      数据资源编码
     * @param excludeDrsId 排除的数据资源ID
     * @return 数据资源数量
     */
    long countDrsCode(Long systemId, String drsCode, Long excludeDrsId);

    /**
     * 插入数据资源
     *
     * @param drs 数据资源
     * @return 数据资源
     */
    DrsEntity insertDrs(DrsEntity drs);

    /**
     * 更新数据资源
     *
     * @param drs 数据资源
     */
    void updateDrs(DrsEntity drs);

    /**
     * 删除数据资源（逻辑删除）
     *
     * @param drsId 数据资源ID
     */
    void deleteDrsById(Long drsId);

    /**
     * 根据数据资源ID统计接口数量
     *
     * @param drsId 数据资源ID
     * @return 接口数量
     */
    long countDrsInterfaces(Long drsId);

    /**
     * 查询数据资源接口列表
     *
     * @param drsId   数据资源ID
     * @param status  状态
     * @param keyword 关键字
     * @return 数据资源接口列表
     */
    List<DrsInterfaceEntity> listDrsInterfaces(Long drsId, Integer status, String keyword);

    /**
     * 根据接口ID查询数据资源接口
     *
     * @param interfaceId 接口ID
     * @return 数据资源接口
     */
    DrsInterfaceEntity findDrsInterfaceById(Long interfaceId);

    /**
     * 根据数据资源ID和接口编码统计数据资源接口数量
     *
     * @param drsId              数据资源ID
     * @param interfaceCode      接口编码
     * @param excludeInterfaceId 排除的接口ID
     * @return 数据资源接口数量
     */
    long countDrsInterfaceCode(Long drsId, String interfaceCode, Long excludeInterfaceId);

    /**
     * 插入数据资源接口
     *
     * @param drsInterface 数据资源接口
     * @return 数据资源接口
     */
    DrsInterfaceEntity insertDrsInterface(DrsInterfaceEntity drsInterface);

    /**
     * 更新数据资源接口
     *
     * @param drsInterface 数据资源接口
     */
    void updateDrsInterface(DrsInterfaceEntity drsInterface);

    /**
     * 删除数据资源接口（逻辑删除）
     *
     * @param interfaceId 接口ID
     */
    void deleteDrsInterfaceById(Long interfaceId);
}
