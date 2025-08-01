package vn.minhtung.ads.domain.response.ad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.minhtung.ads.util.constant.StatusEnum;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
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

    private StatusEnum status;
    private BigDecimal budgetTotal;
    private String category;

    private Instant updatedAt;
    private String updatedBy;
}
