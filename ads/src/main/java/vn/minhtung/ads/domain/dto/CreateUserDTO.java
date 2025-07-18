package vn.minhtung.ads.domain.dto;

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
public class CreateUserDTO {

    private long id;

    private String name;

    private String email;

    private int age;

    private GenderEnum gender;

    private String address;

    private Instant createdAt;

}
