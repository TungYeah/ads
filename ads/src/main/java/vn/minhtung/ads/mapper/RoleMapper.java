package vn.minhtung.ads.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.minhtung.ads.domain.Role;
import vn.minhtung.ads.domain.Permission;
import vn.minhtung.ads.domain.response.role.RoleDTO;
import vn.minhtung.ads.domain.response.role.RoleDTO.PermissionDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(source = "permissions", target = "permissions")
    RoleDTO toRoleDTO(Role role);

    List<RoleDTO> toRoleDTOs(List<Role> roles);

    default PermissionDTO toPermissionDTO(Permission permission) {
        if (permission == null) {
            return null;
        }
        return new PermissionDTO(permission.getId(), permission.getName());
    }
}
