package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;

class DTOMapperUnitTest {

    @Test
    void entityToGetDTO_and_back() {
        User u = new User();
        u.setId(5L);
        u.setUsername("alice");

        UserGetDTO dto = DTOMapper.INSTANCE.convertEntityToUserGetDTO(u);
        assertEquals(5L, dto.getId());
        assertEquals("alice", dto.getUsername());

        UserUpdateDTO upd = new UserUpdateDTO();
        upd.setUsername("bob");
        User back = DTOMapper.INSTANCE.convertUserUpdateDTOtoEntity(upd);
        assertEquals("bob", back.getUsername());
    }
}
