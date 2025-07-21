package vn.minhtung.ads.domain.response.ad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAdDTO {

    private Long id;

    private String title;

    private String description;

    private String imageUrl;

    private String targetUrl;

    private Instant startDate;
    private Instant endDate;

    private Instant updatedAt;

    private String updatedBy;

}
