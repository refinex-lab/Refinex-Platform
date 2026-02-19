package cn.refinex.system.application.service;

import cn.refinex.base.exception.BizException;
import cn.refinex.system.application.assembler.SystemDomainAssembler;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.ValueDTO;
import cn.refinex.system.application.dto.ValueSetDTO;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.ValueEntity;
import cn.refinex.system.domain.model.entity.ValueSetEntity;
import cn.refinex.system.domain.repository.ValueSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static cn.refinex.base.utils.ValueUtils.*;

/**
 * 值集应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class ValueSetApplicationService {

    private final ValueSetRepository valueSetRepository;
    private final SystemDomainAssembler systemDomainAssembler;

    /**
     * 查询值集列表
     *
     * @param command 查询命令
     * @return 值集列表
     */
    public List<ValueSetDTO> listValueSets(QueryValueSetListCommand command) {
        List<ValueSetEntity> entities = valueSetRepository.listValueSets(
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword()
        );
        List<ValueSetDTO> result = new ArrayList<>();
        for (ValueSetEntity entity : entities) {
            result.add(systemDomainAssembler.toValueSetDto(entity));
        }
        return result;
    }

    /**
     * 查询值集详情
     *
     * @param valueSetId 值集ID
     * @return 值集详情
     */
    public ValueSetDTO getValueSet(Long valueSetId) {
        return systemDomainAssembler.toValueSetDto(requireValueSet(valueSetId));
    }

    /**
     * 创建值集
     *
     * @param command 创建命令
     * @return 值集详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ValueSetDTO createValueSet(CreateValueSetCommand command) {
        if (command == null || isBlank(command.getSetCode()) || isBlank(command.getSetName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        String setCode = command.getSetCode().trim();
        if (valueSetRepository.countValueSetCode(setCode, null) > 0) {
            throw new BizException(SystemErrorCode.VALUESET_CODE_DUPLICATED);
        }
        ValueSetEntity entity = new ValueSetEntity();
        entity.setSetCode(setCode);
        entity.setSetName(command.getSetName().trim());
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));
        entity.setDescription(trimToNull(command.getDescription()));
        ValueSetEntity created = valueSetRepository.insertValueSet(entity);
        return systemDomainAssembler.toValueSetDto(created);
    }

    /**
     * 更新值集
     *
     * @param command 更新命令
     * @return 值集详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ValueSetDTO updateValueSet(UpdateValueSetCommand command) {
        if (command == null || command.getValueSetId() == null || isBlank(command.getSetName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        ValueSetEntity existing = requireValueSet(command.getValueSetId());
        existing.setSetName(command.getSetName().trim());
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));
        existing.setDescription(trimToNull(command.getDescription()));
        valueSetRepository.updateValueSet(existing);
        return systemDomainAssembler.toValueSetDto(requireValueSet(existing.getId()));
    }

    /**
     * 删除值集
     *
     * @param valueSetId 值集ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteValueSet(Long valueSetId) {
        ValueSetEntity valueSet = requireValueSet(valueSetId);
        if (valueSetRepository.countValuesBySetCode(valueSet.getSetCode()) > 0) {
            throw new BizException(SystemErrorCode.VALUESET_HAS_VALUES);
        }
        valueSetRepository.deleteValueSetById(valueSet.getId());
    }

    /**
     * 查询值集明细列表
     *
     * @param command 查询命令
     * @return 值集明细列表
     */
    public List<ValueDTO> listValues(QueryValueListCommand command) {
        if (command == null || isBlank(command.getSetCode())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        String setCode = command.getSetCode().trim();
        ValueSetEntity valueSet = valueSetRepository.findValueSetByCode(setCode);
        if (valueSet == null || (valueSet.getDeleted() != null && valueSet.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.VALUESET_NOT_FOUND);
        }
        List<ValueEntity> entities = valueSetRepository.listValues(setCode, command.getStatus(), command.getKeyword());
        List<ValueDTO> result = new ArrayList<>();
        for (ValueEntity entity : entities) {
            result.add(systemDomainAssembler.toValueDto(entity));
        }
        return result;
    }

    /**
     * 查询值集明细详情
     *
     * @param valueId 值ID
     * @return 值集明细详情
     */
    public ValueDTO getValue(Long valueId) {
        return systemDomainAssembler.toValueDto(requireValue(valueId));
    }

    /**
     * 创建值集明细
     *
     * @param command 创建命令
     * @return 值集明细详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ValueDTO createValue(CreateValueCommand command) {
        if (command == null || isBlank(command.getSetCode()) || isBlank(command.getValueCode()) || isBlank(command.getValueName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        String setCode = command.getSetCode().trim();
        ValueSetEntity valueSet = valueSetRepository.findValueSetByCode(setCode);
        if (valueSet == null || (valueSet.getDeleted() != null && valueSet.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.VALUESET_NOT_FOUND);
        }
        String valueCode = command.getValueCode().trim();
        if (valueSetRepository.countValueCode(setCode, valueCode, null) > 0) {
            throw new BizException(SystemErrorCode.VALUE_CODE_DUPLICATED);
        }

        ValueEntity entity = new ValueEntity();
        entity.setSetCode(setCode);
        entity.setValueCode(valueCode);
        entity.setValueName(command.getValueName().trim());
        entity.setValueDesc(trimToNull(command.getValueDesc()));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setIsDefault(defaultIfNull(command.getIsDefault(), 0));
        entity.setSort(defaultIfNull(command.getSort(), 0));
        if (entity.getIsDefault() != null && entity.getIsDefault() == 1) {
            valueSetRepository.clearDefaultValue(setCode, null);
        }
        ValueEntity created = valueSetRepository.insertValue(entity);
        return systemDomainAssembler.toValueDto(created);
    }

    /**
     * 更新值集明细
     *
     * @param command 更新命令
     * @return 值集明细详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ValueDTO updateValue(UpdateValueCommand command) {
        if (command == null || command.getValueId() == null || isBlank(command.getValueCode()) || isBlank(command.getValueName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        ValueEntity existing = requireValue(command.getValueId());
        String valueCode = command.getValueCode().trim();
        if (valueSetRepository.countValueCode(existing.getSetCode(), valueCode, existing.getId()) > 0) {
            throw new BizException(SystemErrorCode.VALUE_CODE_DUPLICATED);
        }
        existing.setValueCode(valueCode);
        existing.setValueName(command.getValueName().trim());
        existing.setValueDesc(trimToNull(command.getValueDesc()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setIsDefault(defaultIfNull(command.getIsDefault(), existing.getIsDefault()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));
        if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            valueSetRepository.clearDefaultValue(existing.getSetCode(), existing.getId());
        }
        valueSetRepository.updateValue(existing);
        return systemDomainAssembler.toValueDto(requireValue(existing.getId()));
    }

    /**
     * 删除值集明细
     *
     * @param valueId 值ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteValue(Long valueId) {
        ValueEntity value = requireValue(valueId);
        valueSetRepository.deleteValueById(value.getId());
    }

    /**
     * 获取值集
     *
     * @param valueSetId 值集ID
     * @return 值集
     */
    private ValueSetEntity requireValueSet(Long valueSetId) {
        if (valueSetId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        ValueSetEntity valueSet = valueSetRepository.findValueSetById(valueSetId);
        if (valueSet == null || (valueSet.getDeleted() != null && valueSet.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.VALUESET_NOT_FOUND);
        }
        return valueSet;
    }

    /**
     * 获取值集明细
     *
     * @param valueId 值ID
     * @return 值集明细
     */
    private ValueEntity requireValue(Long valueId) {
        if (valueId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        ValueEntity value = valueSetRepository.findValueById(valueId);
        if (value == null || (value.getDeleted() != null && value.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.VALUE_NOT_FOUND);
        }
        return value;
    }
}
