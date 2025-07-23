package vn.minhtung.ads.domain.response.adView;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdViewDTO {
    private long id;
    private long userId;
    private long adId;
    private Instant viewedAt;
    private String deviceInfo;
    private String ipAddress;
}
