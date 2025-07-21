package vn.minhtung.ads.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import vn.minhtung.ads.domain.Role;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.service.RoleService;
import vn.minhtung.ads.util.anotation.ApiMessage;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role role) throws IdInvalidException {
        Role postRole = this.roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(postRole);
    }

    @GetMapping("/roles")
    public ResponseEntity<ResultPageinationDTO> getAllRole(@Filter Specification<Role> spec, Pageable pageable) {
        return ResponseEntity.ok(this.roleService.getAllRoles(spec, pageable));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<Role> updateRoles(@Valid @RequestBody Role role, @PathVariable long id)
            throws IdInvalidException {
        if (this.roleService.getById(id) == null) {
            throw new IdInvalidException("Role với id không tồn tại");
        }
        if (this.roleService.existByName(role.getName())) {
            throw new IdInvalidException("Role với name đã tồn tại");
        }
        Role updatedRole = this.roleService.update(role, id);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        if (this.roleService.getById(id) == null) {
            throw new IdInvalidException("Role với id không tồn tại");
        }
        this.roleService.delete(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable("id") long id) throws IdInvalidException {
        Role role = this.roleService.getById(id);
        if (role == null) {
            throw new IdInvalidException("Role với id không tồn tại");
        }
        return ResponseEntity.ok().body(role);
    }
}
