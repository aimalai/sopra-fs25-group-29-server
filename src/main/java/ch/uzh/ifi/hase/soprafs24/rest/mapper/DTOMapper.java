package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mappings({
        @Mapping(source = "password", target = "password"),
        @Mapping(source = "username", target = "username")
    })
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "password", target = "password"),
        @Mapping(source = "username", target = "username"),
        @Mapping(source = "status", target = "status"),
        @Mapping(source = "creationDate", target = "creationDate"),
        @Mapping(source = "birthday", target = "birthday"),
        @Mapping(source = "token", target = "token")
    })
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mappings({
        @Mapping(source = "username", target = "username"),
        @Mapping(source = "password", target = "password"),
        @Mapping(source = "birthday", target = "birthday")
    })
    User convertUserUpdateDTOtoEntity(UserUpdateDTO userUpdateDTO);
}
