package vn.minhtung.ads.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.response.ad.CreateAdDTO;
import vn.minhtung.ads.domain.response.ad.ResAdById;
import vn.minhtung.ads.domain.response.ad.UpdateAdDTO;

@Mapper(componentModel = "spring")
public interface AdMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    Ad toEntity(CreateAdDTO dto);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateAdFromDTO(UpdateAdDTO dto, @MappingTarget Ad ad);

    @Mapping(source = "category.name", target = "category")
    CreateAdDTO toCreateAdDTO(Ad ad);

    @Mapping(source = "category.name", target = "category")
    ResAdById toResAdById(Ad ad);

    @Mapping(source = "category.name", target = "category")
    UpdateAdDTO toUpdateAdDTO(Ad ad);
    
    List<CreateAdDTO> toCreateAdDTOs(List<Ad> ads);
}