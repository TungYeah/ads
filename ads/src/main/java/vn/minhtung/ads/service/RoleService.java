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
import vn.minhtung.ads.repository.PermissionRepository;
import vn.minhtung.ads.repository.RoleRepository;


@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public Role createRole(Role r) {
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }
        return this.roleRepository.save(r);
    }

    public ResultPageinationDTO getAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pRole = this.roleRepository.findAll(spec, pageable);
        ResultPageinationDTO rs = new  ResultPageinationDTO();
        ResultPageinationDTO.Meta mt = new  ResultPageinationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pRole.getTotalPages());
        mt.setTotal(pRole.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pRole.getContent());
        return rs;
    }

    public Role getById(long id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if (roleOptional.isPresent())
            return roleOptional.get();
        return null;
    }

    public boolean existByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    public Role update(Role r, long id) {
        Role role = this.getById(id);
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }
        role.setName(r.getName());
        role.setDescription(r.getDescription());
        role.setActive(r.isActive());
        role.setPermissions(r.getPermissions());
        role = this.roleRepository.save(role);
        return role;
    }

    public void delete(long id) {
        this.roleRepository.deleteById(id);
    }

}
