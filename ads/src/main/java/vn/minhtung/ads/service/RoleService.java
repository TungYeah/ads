package vn.minhtung.ads.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.minhtung.ads.domain.Permission;
import vn.minhtung.ads.domain.Role;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.role.RoleDTO;
import vn.minhtung.ads.mapper.RoleMapper;
import vn.minhtung.ads.repository.PermissionRepository;
import vn.minhtung.ads.repository.RoleRepository;
import vn.minhtung.ads.util.PaginationUtil;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
    }

    public Role createRole(Role r) {
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions()
                    .stream().map(Permission::getId)
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }
        return this.roleRepository.save(r);
    }

    public ResultPageinationDTO getAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pRole = this.roleRepository.findAll(spec, pageable);
        List<RoleDTO> dtos = roleMapper.toRoleDTOs(pRole.getContent());
        return PaginationUtil.build(pRole, dtos);
    }

    public Role getById(long id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        return roleOptional.orElse(null);
    }

    public boolean existByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    public Role update(Role r, long id) {
        Role role = this.getById(id);
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions()
                    .stream().map(Permission::getId)
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }

        role.setName(r.getName());
        role.setDescription(r.getDescription());
        role.setActive(r.isActive());
        role.setPermissions(r.getPermissions());

        return this.roleRepository.save(role);
    }

    public void delete(long id) {
        this.roleRepository.deleteById(id);
    }
}
