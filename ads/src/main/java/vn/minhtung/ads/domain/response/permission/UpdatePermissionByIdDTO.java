package vn.minhtung.ads.domain.response.permission;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePermissionByIdDTO {

    private String name;

    private String method;

    private String apiPath;

    private String module;

    private Instant updatedAt;

    private String updatedBy;
}
