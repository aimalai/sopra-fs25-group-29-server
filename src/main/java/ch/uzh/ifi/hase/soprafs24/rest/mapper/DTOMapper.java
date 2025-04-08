package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * Converts internal representation to external API representation and vice versa.
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(target = "id", ignore = true) // Ignore unmapped fields
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)  
    @Mapping(target = "failedLoginAttempts", ignore = true)  
    @Mapping(target = "lockoutUntil", ignore = true)  
    @Mapping(source = "email", target = "email") // NEW: Ensure email is mapped
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "profilePicture", target = "profilePicture")
    @Mapping(source = "createdAt", target = "createdAt")
    UserGetDTO convertEntityToUserGetDTO(User user);
}
