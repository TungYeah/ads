package vn.minhtung.ads.mapper;

import org.mapstruct.Mapper;
import vn.minhtung.ads.domain.Permission;
import vn.minhtung.ads.domain.response.permission.GetPermissionByIdDTO;
import vn.minhtung.ads.domain.response.permission.UpdatePermissionByIdDTO;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    GetPermissionByIdDTO toGetPermissionByIdDTO(Permission permission);

    UpdatePermissionByIdDTO toUpdatePermissionByIdDTO(Permission permission);

}
