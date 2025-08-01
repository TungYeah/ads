package vn.minhtung.ads.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.response.user.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role", target = "role")
    GetUserByIdDTO toGetUserByIdDTO(User user);

    UpdateUserDTO toUpdateUserDTO(User user);

    CreateUserDTO toCreateUserDTO(User user);
}
