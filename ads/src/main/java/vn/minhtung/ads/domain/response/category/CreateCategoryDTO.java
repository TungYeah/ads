package vn.minhtung.ads.domain.response.category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryDTO {

    @NotBlank(message = "Ko dc de trong ten danh muc")
    private String name;
}
