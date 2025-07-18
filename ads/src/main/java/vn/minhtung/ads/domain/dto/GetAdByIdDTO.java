package vn.minhtung.ads.domain.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAdByIdDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String targetUrl;
    private Instant startDate;
    private Instant endDate;

}
