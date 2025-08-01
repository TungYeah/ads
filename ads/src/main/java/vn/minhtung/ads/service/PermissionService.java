package vn.minhtung.ads.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.minhtung.ads.domain.Permission;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.permission.GetPermissionByIdDTO;
import vn.minhtung.ads.domain.response.permission.UpdatePermissionByIdDTO;
import vn.minhtung.ads.mapper.PermissionMapper;
import vn.minhtung.ads.repository.PermissionRepository;
import vn.minhtung.ads.util.PaginationUtil;
import vn.minhtung.ads.util.errors.IdInvalidException;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    public Permission handlePermission(Permission permission) throws IdInvalidException {
        if (permissionRepository.existsByApiPathAndMethodAndModule(permission.getApiPath(), permission.getMethod(),
                permission.getModule())) {
            throw new IdInvalidException("Permission already exists");
        }
        return this.permissionRepository.save(permission);
    }

    public ResultPageinationDTO getAllPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> pagePermission = this.permissionRepository.findAll(spec, pageable);
        List<GetPermissionByIdDTO> list = pagePermission.getContent()
                .stream()
                .map(permissionMapper::toGetPermissionByIdDTO)
                .collect(Collectors.toList());

        return PaginationUtil.build(pagePermission, list);
    }

    @Cacheable(value = "permissions", key = "#id")
    public Permission getPermissionById(long id) throws IdInvalidException {
        return this.permissionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Permission not found with id: " + id));
    }

    @CacheEvict(value = "permissions", key = "#id")
    public Permission updatePermission(long id, Permission updatedPermission) throws IdInvalidException {
        Permission current = this.getPermissionById(id);
        if (current == null) {
            throw new EntityNotFoundException("Permission not found with id: " + id);
        }
        current.setName(updatedPermission.getName());
        current.setApiPath(updatedPermission.getApiPath());
        current.setMethod(updatedPermission.getMethod());
        current.setModule(updatedPermission.getModule());

        return this.permissionRepository.save(current);
    }

    public GetPermissionByIdDTO convertToGetPermissionByIdDTO(Permission permission) {
        return permissionMapper.toGetPermissionByIdDTO(permission);
    }

    public UpdatePermissionByIdDTO convPermissionByIdDTO(Permission permission) {
        return permissionMapper.toUpdatePermissionByIdDTO(permission);
    }

    public void deletePermission(long id) {
        if (!permissionRepository.existsById(id)) {
            throw new NoSuchElementException("Permission not found");
        }
        this.permissionRepository.deleteById(id);
    }
}
