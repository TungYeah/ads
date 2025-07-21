package vn.minhtung.ads.domain.response.category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.minhtung.ads.domain.response.ad.GetAdByIdDTO;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetCategoryByIdDTO {
    private long id;

    private String name;

    private List<GetAdByIdDTO> ads;
}
