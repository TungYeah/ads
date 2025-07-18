package vn.minhtung.ads.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetCategoryByIdDTO {
    private long id;

    private String name;

    private List<GetAdByIdDTO> ads;
}
