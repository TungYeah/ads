package vn.minhtung.ads.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import vn.minhtung.ads.domain.Permission;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO.Meta;
import vn.minhtung.ads.domain.response.permission.GetPermissionByIdDTO;
import vn.minhtung.ads.domain.response.permission.UpdatePermissionByIdDTO;
import vn.minhtung.ads.repository.PermissionRepository;
import vn.minhtung.ads.util.errors.IdInvalidException;

@Service
public class PermissionService {

    private final PermissionRepository permissionReposity;

    public PermissionService(PermissionRepository permissionReposity) {
        this.permissionReposity = permissionReposity;
    }
    

    public Permission handlePermission(Permission permission) throws IdInvalidException {
        if (permissionReposity.existsByApiPathAndMethodAndModule(permission.getApiPath(), permission.getMethod(),
                permission.getModule())) {
            throw new IdInvalidException("Permission already exits");
        }
        return this.permissionReposity.save(permission);
    }

    public ResultPageinationDTO getAllPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> pagePermission = this.permissionReposity.findAll(spec, pageable);
        ResultPageinationDTO rs = new ResultPageinationDTO();
        Meta mt = new Meta();

        mt.setPage(pagePermission.getNumber() + 1);
        mt.setPageSize(pagePermission.getSize());
        mt.setPages(pagePermission.getTotalPages());
        mt.setTotal(pagePermission.getTotalElements());
        rs.setMeta(mt);

        List<GetPermissionByIdDTO> listPermissions = pagePermission.getContent()
                .stream()
                .map(permission -> new GetPermissionByIdDTO(
                        permission.getId(),
                        permission.getName(),
                        permission.getApiPath(),
                        permission.getMethod(),
                        permission.getModule(),
                        permission.getCreatedAt(),
                        permission.getCreatedBy(),
                        permission.getUpdatedAt(),
                        permission.getUpdatedBy()))
                .collect(Collectors.toList());

        rs.setResult(listPermissions);
        return rs;
    }

    public Permission getPermissionById(long id) throws IdInvalidException {
        return this.permissionReposity.findById(id)
                .orElseThrow(() -> new IdInvalidException("Permission not found with id: "));
    }

    public Permission updatePermission(long id, Permission updatedPermission) throws IdInvalidException {
        Permission currentPermission = this.getPermissionById(id);
        if (currentPermission == null) {
            throw new EntityNotFoundException("Permission not found with id: " + id);
        }
        currentPermission.setName(updatedPermission.getName());
        currentPermission.setApiPath(updatedPermission.getApiPath());
        currentPermission.setMethod(updatedPermission.getMethod());
        currentPermission.setModule(updatedPermission.getModule());

        return this.permissionReposity.save(currentPermission);
    }

    public GetPermissionByIdDTO convertToGetPermissionByIdDTO(Permission permission) {
        return new GetPermissionByIdDTO(
                permission.getId(),
                permission.getName(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getCreatedAt(),
                permission.getCreatedBy(),
                permission.getUpdatedAt(),
                permission.getUpdatedBy());
    }

    public UpdatePermissionByIdDTO convPermissionByIdDTO(Permission permission) {
        return new UpdatePermissionByIdDTO(
                permission.getName(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getUpdatedAt(),
                permission.getUpdatedBy());
    }

     public void deletePermission(long id) {
        if (!permissionReposity.existsById(id)) {
            throw new NoSuchElementException("User not found");
        }
        this.permissionReposity.deleteById(id);
    }

}
