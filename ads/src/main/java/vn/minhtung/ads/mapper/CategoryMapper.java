package vn.minhtung.ads.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.minhtung.ads.domain.Category;
import vn.minhtung.ads.domain.response.category.GetCategoryByIdDTO;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "ads", target = "ads")
    GetCategoryByIdDTO toGetCategoryByIdDTO(Category category);

    List<GetCategoryByIdDTO> toGetCategoryByIdDTOs(List<Category> categories);
}
