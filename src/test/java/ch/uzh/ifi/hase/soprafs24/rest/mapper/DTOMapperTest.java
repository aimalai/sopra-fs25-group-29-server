package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation works.
 */
public class DTOMapperTest {
    @Test
    public void testCreateUser_fromUserPostDTO_toUser_success() {
        // Create UserPostDTO
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUser");
        userPostDTO.setPassword("StrongPass@123");
        userPostDTO.setEmail("test@example.com");

        // MAP -> Create User
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // Check content
        assertEquals(userPostDTO.getUsername(), user.getUsername());
        assertEquals(userPostDTO.getPassword(), user.getPassword());
        assertEquals(userPostDTO.getEmail(), user.getEmail());
    }

    @Test
    public void testGetUser_fromUser_toUserGetDTO_success() {
        // Create User
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setProfilePicture("profile_picture_url");
        user.setCreatedAt(java.time.LocalDateTime.now());

        // MAP -> Create UserGetDTO
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // Check content
        assertEquals(user.getId(), userGetDTO.getId());
        assertEquals(user.getUsername(), userGetDTO.getUsername());
        assertEquals(user.getEmail(), userGetDTO.getEmail());
        assertEquals(user.getProfilePicture(), userGetDTO.getProfilePicture());
        assertEquals(user.getCreatedAt(), userGetDTO.getCreatedAt());
    }
}
