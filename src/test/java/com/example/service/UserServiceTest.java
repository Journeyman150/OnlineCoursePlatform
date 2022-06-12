package com.example.service;

import com.example.dao.UserDAO;
import com.example.domain.User;
import com.example.search_engine.IndexedData;
import com.example.search_engine.UsersSearchData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @MockBean
    private Authentication authentication;
    @MockBean
    private UserDAO userDAO;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UsersSearchData usersSearchData;

    @Test
    public void loadUserByUsername() {
        User user = new User();
        user.setEmail("test@test.ru");
        BDDMockito.given(userDAO.getUserByEmail("test@test.ru")).willReturn(user);
        BDDMockito.given(userDAO.getUserByEmail("")).willThrow(UsernameNotFoundException.class);

        assertNotNull(userService.loadUserByUsername("test@test.ru"));
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(""));
    }

    @Test
    public void findUserId() {
        User user = new User();
        user.setEmail("test@test.ru");
        user.setId(1L);
        BDDMockito.given(userDAO.getUserByEmail("test@test.ru")).willReturn(user);

        assertEquals(Long.valueOf(1L), userService.findUserId("test@test.ru"));
        assertNull(userService.findUserId(""));
    }

    @Test
    public void getUserById() {
        User user = new User();
        user.setId(1L);
        BDDMockito.given(userDAO.getUserById(1L)).willReturn(user);

        assertSame(user, userService.getUserById(1L));
    }

    @Test
    public void getAuthorizedUser() {
        User user = new User();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        BDDMockito.given(authentication.getPrincipal()).willReturn(user);
        User sameUser = userService.getAuthorizedUser();

        BDDMockito.verify(authentication).getPrincipal();
        assertSame(user, sameUser);
    }

    @Test
    public void isUserAlreadyExist() {
        User user = new User();
        user.setEmail("test@test.ru");
        BDDMockito.given(userDAO.getUserByEmail("test@test.ru")).willReturn(user);

        assertTrue(userService.isUserAlreadyExist("test@test.ru"));
        assertFalse(userService.isUserAlreadyExist(""));
    }

    @Test
    public void getUsersList() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = Arrays.asList(user1, user2);
        BDDMockito.given(userDAO.getUsersList()).willReturn(users);

        assertSame(users, userService.getUsersList());
    }

    @Test
    public void getFilteredUsersList() {
        Set<Long> idxes = Set.of(1L, 2L);
        User user1 = new User();
        User user2 = new User();
        user1.setId(1L);
        user2.setId(2L);
        BDDMockito.given(usersSearchData.findIndexes("name surname")).willReturn(idxes);
        BDDMockito.given(userDAO.getUserById(1L)).willReturn(user1);
        BDDMockito.given(userDAO.getUserById(2L)).willReturn(user2);

        assertEquals(2, userService.getFilteredUsersList("name surname").size());
        assertNotNull(userService.getFilteredUsersList("name surname")
                .stream().filter(n -> n.getId() == 1L).findAny().orElse(null));
        assertNotNull(userService.getFilteredUsersList("name surname")
                .stream().filter(n -> n.getId() == 2L).findAny().orElse(null));
        assertEquals(0, userService.getFilteredUsersList("").size());
    }

    @Test
    public void save() {
        User user = new User();
        user.setPassword("password");
        BDDMockito.given(passwordEncoder.encode(user.getPassword())).willReturn("encodedPassword");
        BDDMockito.given(userDAO.save(user)).willReturn(1L);

        assertEquals(1L, userService.save(user));
        assertEquals("encodedPassword", user.getPassword());

        BDDMockito.verify(usersSearchData, Mockito.times(1))
                .writeData(1L, user.getEmail(), user.getName(), user.getSurname());
    }

    @Test
    public void update() {
        User updatedUser = new User();
        updatedUser.setEmail("updatedEmail");
        User oldUser = new User();
        oldUser.setEmail("oldEmail");
        long userId = 1;
        BDDMockito.given(userDAO.getUserById(userId)).willReturn(oldUser);
        userService.update(userId, updatedUser);

        BDDMockito.verify(usersSearchData, Mockito.times(1))
                .deleteData(userId, oldUser.getEmail(), oldUser.getName(), oldUser.getSurname());
        BDDMockito.verify(usersSearchData, Mockito.times(1))
                .writeData(userId, updatedUser.getEmail(), updatedUser.getName(), updatedUser.getSurname());
        BDDMockito.verify(userDAO, Mockito.times(1)).updateUser(userId, updatedUser);
    }

    @Test
    public void changePassword() {
        long userId = 1L;
        String password = "password";
        BDDMockito.given(passwordEncoder.encode(password)).willReturn("encodedPassword");

        userService.changePassword(userId, password);
        BDDMockito.verify(passwordEncoder).encode(password);
        BDDMockito.verify(userDAO).changeUserPassword(userId, "encodedPassword");
    }

    @Test
    public void changeRole() {
        userService.changeRole(1L, "USER");
        BDDMockito.verify(userDAO).changeUserRole(1L, "USER");
    }

    @Test
    public void updateUserBalance() {
        userService.updateUserBalance(1L, 1000);
        BDDMockito.verify(userDAO).updateUserBalance(1L, 1000);
    }

    @Test
    public void delete() {
        long userId = 1L;
        User user = new User();
        user.setId(1L);
        BDDMockito.given(userDAO.getUserById(1L)).willReturn(user);

        userService.delete(userId);
        BDDMockito.verify(usersSearchData).deleteData(userId, user.getEmail(), user.getName(), user.getSurname());
        BDDMockito.verify(userDAO).deleteUser(userId);
    }

    @Test
    public void passwordMatches() {
    }

    @Test
    public void getSearchDataList() {
        List<IndexedData> indexedData = new ArrayList<>();
        BDDMockito.given(userDAO.getSearchDataList()).willReturn(indexedData);
        assertSame(indexedData, userService.getSearchDataList());
    }
}