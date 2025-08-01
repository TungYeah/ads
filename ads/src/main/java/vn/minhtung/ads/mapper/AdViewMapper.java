package vn.minhtung.ads.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.minhtung.ads.domain.AdView;
import vn.minhtung.ads.domain.response.adView.AdViewDTO;

@Mapper(componentModel = "spring")
public interface AdViewMapper {

    @Mapping(source = "ad.id", target = "adId")
    @Mapping(source = "user.id", target = "userId")
    AdViewDTO toDTO(AdView adView);

    @Mapping(source = "adId", target = "ad.id")
    @Mapping(source = "userId", target = "user.id")
    AdView toEntity(AdViewDTO dto);

    List<AdViewDTO> toDTOs(List<AdView> adViews);
}
