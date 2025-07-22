package vn.minhtung.ads.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.minhtung.ads.domain.Permission;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.permission.GetPermissionByIdDTO;
import vn.minhtung.ads.domain.response.permission.UpdatePermissionByIdDTO;
import vn.minhtung.ads.service.PermissionService;
import vn.minhtung.ads.util.anotation.ApiMessage;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission)
            throws IdInvalidException {
        Permission postPermission = this.permissionService.handlePermission(permission);
        return ResponseEntity.status(HttpStatus.CREATED).body(postPermission);
    }

    @GetMapping("/permissions")
    public ResponseEntity<ResultPageinationDTO> getAllPermissions(
            @Filter Specification<Permission> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.permissionService.getAllPermissions(spec, pageable));
    }

    @GetMapping("/permissions/{id}")
    public ResponseEntity<GetPermissionByIdDTO> getPermissionById(@PathVariable long id) throws IdInvalidException {
        Permission permission = this.permissionService.getPermissionById(id);
        if (permission == null) {
            throw new IdInvalidException("Không tìm thấy id quyền: " + id);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.permissionService.convertToGetPermissionByIdDTO(permission));
    }

    @PutMapping("/permissions/{id}")
    public ResponseEntity<UpdatePermissionByIdDTO> updatePermission(
            @PathVariable long id,
            @Valid @RequestBody Permission updateData) throws IdInvalidException {

        Permission existingPermission = this.permissionService.getPermissionById(id);
        if (existingPermission == null) {
            throw new IdInvalidException("Không tìm thấy id ");
        }
        Permission updatedPermission = this.permissionService.updatePermission(id, updateData);
        return ResponseEntity.status(HttpStatus.OK).body(
                this.permissionService.convPermissionByIdDTO(updatedPermission));
    }

  @DeleteMapping("/permissions/{id}")
  @ApiMessage("Xoa Permission thanh cong")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") long id) throws IdInvalidException {
        Permission curretPermission = this.permissionService.getPermissionById(id);
        if (curretPermission == null) {
            throw new IdInvalidException("User khong ton tai Id");
        }
        permissionService.deletePermission(id);
        return ResponseEntity.ok(null);
    }

}
