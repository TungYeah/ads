package vn.minhtung.ads.domain.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.minhtung.ads.util.constant.GenderEnum;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {

    private long id;

    private String name;

    private int age;

    private GenderEnum gender;

    private String address;

    private Instant updatedAt;

    private String updatedBy;
}
