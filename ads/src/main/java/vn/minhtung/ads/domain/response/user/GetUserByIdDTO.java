package vn.minhtung.ads.domain.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.minhtung.ads.domain.response.ad.GetAdByIdDTO;
import vn.minhtung.ads.util.constant.GenderEnum;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetUserByIdDTO {
    private long id;

    private String name;

    private String email;

    private int age;

    private GenderEnum gender;

    private String address;

    private Instant createdAt;

    private Instant updateAt;

    private List<GetAdByIdDTO> ads;

}
