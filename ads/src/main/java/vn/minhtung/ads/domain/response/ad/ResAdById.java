package vn.minhtung.ads.domain.response.ad;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResAdById {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String targetUrl;
    private Instant startDate;
    private Instant endDate;
    private String category;
}
