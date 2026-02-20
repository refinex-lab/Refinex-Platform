package cn.refinex.system.application.service;

import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.base.utils.UniqueCodeUtils;
import cn.refinex.system.application.assembler.SystemDomainAssembler;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.DrsDTO;
import cn.refinex.system.application.dto.DrsInterfaceDTO;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.DrsEntity;
import cn.refinex.system.domain.model.entity.DrsInterfaceEntity;
import cn.refinex.system.domain.model.entity.SystemEntity;
import cn.refinex.system.domain.repository.DataResourceRepository;
import cn.refinex.system.domain.repository.SystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static cn.refinex.base.utils.ValueUtils.*;

/**
 * 数据资源应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class DataResourceApplicationService {

    private static final int MAX_CODE_GENERATE_ATTEMPTS = 10;

    private final DataResourceRepository dataResourceRepository;
    private final SystemRepository systemRepository;
    private final SystemDomainAssembler systemDomainAssembler;

    /**
     * 查询数据资源列表
     *
     * @param command 查询命令
     * @return 数据资源列表
     */
    public PageResponse<DrsDTO> listDrs(QueryDrsListCommand command) {
        if (command != null && command.getSystemId() != null) {
            requireSystem(command.getSystemId());
        }
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<DrsEntity> entities = dataResourceRepository.listDrs(
                command == null ? null : command.getSystemId(),
                command == null ? null : command.getStatus(),
                command == null ? null : command.getDrsType(),
                command == null ? null : command.getOwnerEstabId(),
                command == null ? null : command.getKeyword(),
                currentPage,
                pageSize
        );
        List<DrsDTO> result = new ArrayList<>();
        for (DrsEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toDrsDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询数据资源详情
     *
     * @param drsId 数据资源ID
     * @return 数据资源详情
     */
    public DrsDTO getDrs(Long drsId) {
        return systemDomainAssembler.toDrsDto(requireDrs(drsId));
    }

    /**
     * 创建数据资源
     *
     * @param command 创建命令
     * @return 数据资源详情
     */
    @Transactional(rollbackFor = Exception.class)
    public DrsDTO createDrs(CreateDrsCommand command) {
        if (command == null
                || command.getSystemId() == null
                || isBlank(command.getDrsName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireSystem(command.getSystemId());
        String drsCode = generateDrsCode(command.getSystemId(), command.getDrsCode());
        DrsEntity entity = new DrsEntity();
        entity.setSystemId(command.getSystemId());
        entity.setDrsCode(drsCode);
        entity.setDrsName(command.getDrsName().trim());
        entity.setDrsType(defaultIfNull(command.getDrsType(), 0));
        entity.setResourceUri(trimToNull(command.getResourceUri()));
        entity.setOwnerEstabId(defaultIfNull(command.getOwnerEstabId(), 0L));
        entity.setDataOwnerType(defaultIfNull(command.getDataOwnerType(), 0));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setRemark(trimToNull(command.getRemark()));
        DrsEntity created = dataResourceRepository.insertDrs(entity);
        return systemDomainAssembler.toDrsDto(created);
    }

    /**
     * 更新数据资源
     *
     * @param command 更新命令
     * @return 数据资源详情
     */
    @Transactional(rollbackFor = Exception.class)
    public DrsDTO updateDrs(UpdateDrsCommand command) {
        if (command == null || command.getDrsId() == null || isBlank(command.getDrsName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        DrsEntity existing = requireDrs(command.getDrsId());
        existing.setDrsName(command.getDrsName().trim());
        existing.setDrsType(defaultIfNull(command.getDrsType(), existing.getDrsType()));
        existing.setResourceUri(trimToNull(command.getResourceUri()));
        existing.setOwnerEstabId(defaultIfNull(command.getOwnerEstabId(), existing.getOwnerEstabId()));
        existing.setDataOwnerType(defaultIfNull(command.getDataOwnerType(), existing.getDataOwnerType()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setRemark(trimToNull(command.getRemark()));
        dataResourceRepository.updateDrs(existing);
        return systemDomainAssembler.toDrsDto(requireDrs(existing.getId()));
    }

    /**
     * 删除数据资源
     *
     * @param drsId 数据资源ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDrs(Long drsId) {
        DrsEntity drs = requireDrs(drsId);
        if (dataResourceRepository.countDrsInterfaces(drs.getId()) > 0) {
            throw new BizException(SystemErrorCode.DRS_HAS_INTERFACES);
        }
        dataResourceRepository.deleteDrsById(drs.getId());
    }

    /**
     * 查询数据资源接口列表
     *
     * @param command 查询命令
     * @return 数据资源接口列表
     */
    public PageResponse<DrsInterfaceDTO> listDrsInterfaces(QueryDrsInterfaceListCommand command) {
        if (command == null || command.getDrsId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireDrs(command.getDrsId());
        int currentPage = PageUtils.normalizeCurrentPage(command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<DrsInterfaceEntity> entities = dataResourceRepository.listDrsInterfaces(
                command.getDrsId(),
                command.getStatus(),
                command.getKeyword(),
                currentPage,
                pageSize
        );
        List<DrsInterfaceDTO> result = new ArrayList<>();
        for (DrsInterfaceEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toDrsInterfaceDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询数据资源接口详情
     *
     * @param interfaceId 接口ID
     * @return 数据资源接口详情
     */
    public DrsInterfaceDTO getDrsInterface(Long interfaceId) {
        return systemDomainAssembler.toDrsInterfaceDto(requireDrsInterface(interfaceId));
    }

    /**
     * 创建数据资源接口
     *
     * @param command 创建命令
     * @return 数据资源接口详情
     */
    @Transactional(rollbackFor = Exception.class)
    public DrsInterfaceDTO createDrsInterface(CreateDrsInterfaceCommand command) {
        if (command == null
                || command.getDrsId() == null
                || isBlank(command.getInterfaceName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        DrsEntity drs = requireDrs(command.getDrsId());
        String interfaceCode = generateDrsInterfaceCode(drs.getId(), command.getInterfaceCode());
        DrsInterfaceEntity entity = new DrsInterfaceEntity();
        entity.setDrsId(drs.getId());
        entity.setInterfaceCode(interfaceCode);
        entity.setInterfaceName(command.getInterfaceName().trim());
        entity.setHttpMethod(trimToNull(command.getHttpMethod()));
        entity.setPathPattern(trimToNull(command.getPathPattern()));
        entity.setPermissionKey(trimToNull(command.getPermissionKey()));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));
        DrsInterfaceEntity created = dataResourceRepository.insertDrsInterface(entity);
        return systemDomainAssembler.toDrsInterfaceDto(created);
    }

    /**
     * 更新数据资源接口
     *
     * @param command 更新命令
     * @return 数据资源接口详情
     */
    @Transactional(rollbackFor = Exception.class)
    public DrsInterfaceDTO updateDrsInterface(UpdateDrsInterfaceCommand command) {
        if (command == null
                || command.getInterfaceId() == null
                || isBlank(command.getInterfaceCode())
                || isBlank(command.getInterfaceName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        DrsInterfaceEntity existing = requireDrsInterface(command.getInterfaceId());
        String interfaceCode = command.getInterfaceCode().trim();
        if (dataResourceRepository.countDrsInterfaceCode(existing.getDrsId(), interfaceCode, existing.getId()) > 0) {
            throw new BizException(SystemErrorCode.DRS_INTERFACE_CODE_DUPLICATED);
        }
        existing.setInterfaceCode(interfaceCode);
        existing.setInterfaceName(command.getInterfaceName().trim());
        existing.setHttpMethod(trimToNull(command.getHttpMethod()));
        existing.setPathPattern(trimToNull(command.getPathPattern()));
        existing.setPermissionKey(trimToNull(command.getPermissionKey()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));
        dataResourceRepository.updateDrsInterface(existing);
        return systemDomainAssembler.toDrsInterfaceDto(requireDrsInterface(existing.getId()));
    }

    /**
     * 删除数据资源接口
     *
     * @param interfaceId 接口ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDrsInterface(Long interfaceId) {
        DrsInterfaceEntity drsInterface = requireDrsInterface(interfaceId);
        dataResourceRepository.deleteDrsInterfaceById(drsInterface.getId());
    }

    /**
     * 生成数据资源编码
     *
     * @param systemId 系统ID
     * @param code     指定编码
     * @return 可用编码
     */
    private String generateDrsCode(Long systemId, String code) {
        if (!isBlank(code)) {
            String normalized = code.trim();
            if (dataResourceRepository.countDrsCode(systemId, normalized, null) > 0) {
                throw new BizException(SystemErrorCode.DRS_CODE_DUPLICATED);
            }
            return normalized;
        }
        for (int i = 0; i < MAX_CODE_GENERATE_ATTEMPTS; i++) {
            String candidate = UniqueCodeUtils.randomUpperCode("DRS_", 8);
            if (dataResourceRepository.countDrsCode(systemId, candidate, null) == 0) {
                return candidate;
            }
        }
        throw new BizException("自动生成数据资源编码失败，请稍后重试", SystemErrorCode.DRS_CODE_DUPLICATED);
    }

    /**
     * 生成数据资源接口编码
     *
     * @param drsId 数据资源ID
     * @param code  指定编码
     * @return 可用编码
     */
    private String generateDrsInterfaceCode(Long drsId, String code) {
        if (!isBlank(code)) {
            String normalized = code.trim();
            if (dataResourceRepository.countDrsInterfaceCode(drsId, normalized, null) > 0) {
                throw new BizException(SystemErrorCode.DRS_INTERFACE_CODE_DUPLICATED);
            }
            return normalized;
        }
        for (int i = 0; i < MAX_CODE_GENERATE_ATTEMPTS; i++) {
            String candidate = UniqueCodeUtils.randomUpperCode("API_", 8);
            if (dataResourceRepository.countDrsInterfaceCode(drsId, candidate, null) == 0) {
                return candidate;
            }
        }
        throw new BizException("自动生成数据资源接口编码失败，请稍后重试", SystemErrorCode.DRS_INTERFACE_CODE_DUPLICATED);
    }

    /**
     * 获取数据资源
     *
     * @param drsId 数据资源ID
     * @return 数据资源
     */
    private DrsEntity requireDrs(Long drsId) {
        if (drsId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        DrsEntity drs = dataResourceRepository.findDrsById(drsId);
        if (drs == null || (drs.getDeleted() != null && drs.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.DRS_NOT_FOUND);
        }
        return drs;
    }

    /**
     * 获取数据资源接口
     *
     * @param interfaceId 接口ID
     * @return 数据资源接口
     */
    private DrsInterfaceEntity requireDrsInterface(Long interfaceId) {
        if (interfaceId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        DrsInterfaceEntity drsInterface = dataResourceRepository.findDrsInterfaceById(interfaceId);
        if (drsInterface == null || (drsInterface.getDeleted() != null && drsInterface.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.DRS_INTERFACE_NOT_FOUND);
        }
        return drsInterface;
    }

    /**
     * 获取系统
     *
     * @param systemId 系统ID
     */
    private void requireSystem(Long systemId) {
        SystemEntity system = systemRepository.findSystemById(systemId);
        if (system == null || (system.getDeleted() != null && system.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.SYSTEM_NOT_FOUND);
        }
    }
}
