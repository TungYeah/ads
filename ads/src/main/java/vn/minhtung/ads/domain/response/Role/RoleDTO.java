package vn.minhtung.ads.domain.response.Role;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.minhtung.ads.domain.Permission;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private long id;
    private String name;

    private List<PermissionDTO> permissions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionDTO {
        private Long id;
        private String name;
    }
}
